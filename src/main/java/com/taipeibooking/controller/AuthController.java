 package com.taipeibooking.controller;

 import com.taipeibooking.dto.LoginRequest;
 import com.taipeibooking.dto.RegisterRequest;
 import com.taipeibooking.model.User;
 import com.taipeibooking.repository.UserRepository;
 import com.taipeibooking.service.AuthService;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.web.bind.annotation.*;
 import com.taipeibooking.security.JwtTokenProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;


 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Optional;

 @RestController
 @RequestMapping("/api/user")
 @CrossOrigin
 public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokenProvider;
  private final UserRepository userRepository;

  @Autowired
  public AuthController(AuthService authService,
                AuthenticationManager authenticationManager,
                JwtTokenProvider tokenProvider,
                UserRepository userRepository) {
   this.authService = authService;
   this.authenticationManager = authenticationManager;
   this.tokenProvider = tokenProvider;
   this.userRepository = userRepository;
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
   if (request.getName() == null || request.getName().isBlank() ||
     request.getEmail() == null || request.getEmail().isBlank() ||
     request.getPassword() == null || request.getPassword().isBlank()) {
    return ResponseEntity.badRequest().body(Map.of("error", true, "message", "註冊失敗，姓名、Email和密碼皆為必填"));
   }

   boolean success = authService.registerUser(request);
   if (success) {
    return ResponseEntity.ok(Map.of("ok", true));
   } else {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", true, "message", "註冊失敗，此 Email 已被註冊"));
   }
  }

  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
   if (request.getEmail() == null || request.getEmail().isBlank() ||
     request.getPassword() == null || request.getPassword().isBlank()) {
    return ResponseEntity.badRequest().body(Map.of("error", true, "message", "登入失敗，Email和密碼皆為必填"));
   }

   try {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.getEmail(),
        request.getPassword()
      )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.generateToken(authentication);

    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new RuntimeException("Authenticated user not found in database: " + username));

    String role = user.getRole();

    logger.info("使用者 '{}' (Role: {}) 登入成功，已生成 JWT。", request.getEmail(), role);

    Map<String, Object> responseBody = new LinkedHashMap<>();
    responseBody.put("token", jwt);
    responseBody.put("role", role);
    return ResponseEntity.ok(responseBody);

   } catch (AuthenticationException e) {
    logger.warn("使用者 '{}' 登入失敗：{}", request.getEmail(), e.getMessage());
    SecurityContextHolder.clearContext();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", true, "message", "登入失敗，Email或密碼錯誤"));
   } catch (Exception e) {
    logger.error("登入過程中發生未知錯誤，使用者: {}", request.getEmail(), e);
    SecurityContextHolder.clearContext();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", true, "message", "伺服器內部錯誤，登入失敗"));
   }
  }


  @GetMapping("/auth")
  public ResponseEntity<?> getUserAuth() {
   Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

   if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
    String username = authentication.getName();
    Optional<User> userOptional = userRepository.findByUsername(username);

    if (userOptional.isPresent()) {
     User user = userOptional.get();

     Map<String, Object> userData = new LinkedHashMap<>();
     userData.put("id", user.getId());
     userData.put("name", user.getName());
     userData.put("email", user.getUsername());
     userData.put("role", user.getRole());

     return ResponseEntity.ok(Map.of("data", userData));
    } else {
     logger.error("錯誤：已認證的使用者在資料庫中找不到: {}", username);
     return ResponseEntity.ok(Map.of("data", null));
    }
   } else {
    return ResponseEntity.ok(Map.of("data", null));
   }
  }
 }
