package com.taipeibooking.service;

import com.taipeibooking.model.User;
import com.taipeibooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("找不到 Email 為 " + email + " 的用戶");
                });

        String role = user.getRole();
        if (!StringUtils.hasText(role)) {
            logger.warn("User {} has an empty or null role. Assigning default 'USER' role.", email);
            role = "USER";
        }

        String authorityString = "ROLE_" + role.toUpperCase();
        logger.debug("Assigning authority: {} to user: {}", authorityString, email);

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(authorityString)
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
