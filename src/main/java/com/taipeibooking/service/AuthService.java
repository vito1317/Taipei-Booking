package com.taipeibooking.service;

import com.taipeibooking.dto.LoginRequest;
import com.taipeibooking.dto.RegisterRequest;
import com.taipeibooking.model.User;
import com.taipeibooking.repository.UserRepository;
import com.taipeibooking.security.JwtTokenProvider; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager; 
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; 
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.AuthenticationException; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class); 

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; 
    private final JwtTokenProvider tokenProvider; 

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, 
                       JwtTokenProvider tokenProvider 
                       ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager; 
        this.tokenProvider = tokenProvider; 
    }

    
    @Transactional
    public boolean registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getEmail())) {
            logger.warn("註冊失敗：Email '{}' 已被使用。", request.getEmail());
            return false; 
        }
        User newUser = new User(
                request.getName(),
                request.getEmail(), 
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(newUser);
        logger.info("使用者 '{}' 註冊成功。", request.getEmail());
        return true;
    }

    
    public Optional<String> loginUser(LoginRequest request) {
        try {
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            
            
            
            SecurityContextHolder.getContext().setAuthentication(authentication);

            
            String jwt = tokenProvider.generateToken(authentication);
            logger.info("使用者 '{}' 登入成功，已生成 JWT。", request.getEmail());

            
            return Optional.of(jwt);

        } catch (AuthenticationException e) {
            
            logger.warn("使用者 '{}' 登入失敗：{}", request.getEmail(), e.getMessage());
            
            SecurityContextHolder.clearContext();
            
            return Optional.empty();
        } catch (Exception e) {
            
             logger.error("登入過程中發生未知錯誤，使用者: {}", request.getEmail(), e);
             SecurityContextHolder.clearContext();
             return Optional.empty();
        }
    }

    
    public Optional<User> getUserByUsername(String username) {
         return userRepository.findByUsername(username);
    }
}