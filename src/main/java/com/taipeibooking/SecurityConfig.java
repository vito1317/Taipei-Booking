package com.taipeibooking;

import com.taipeibooking.repository.UserRepository;
import com.taipeibooking.security.JwtRequestFilter;
import com.taipeibooking.model.User;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, UserRepository userRepository) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + username));
            List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(user.getRole());
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    authorities
            );
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/user/register", "/api/user/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/attractions", "/api/attractions/**").permitAll()
                .requestMatchers("/static/**", "/*.html", "/*.js", "/*.css", "/favicon.ico", "/image/**", "/panel/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/user/auth").authenticated()
                .requestMatchers("/api/booking", "/api/booking/**").authenticated()
                .requestMatchers("/api/admin/attractions", "/api/admin/attractions/**").hasAnyRole("ADMIN", "TRIP_MANAGER")
                .requestMatchers("/api/admin/users", "/api/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/bookings", "/api/admin/bookings/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/stats").hasRole("ADMIN")
                .requestMatchers("/api/admin/trends/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://127.0.0.1:3000", "http://localhost:3000",
                "http://127.0.0.1:5500", "http://localhost:5500",
                "http://127.0.0.1:8080", "http://localhost:8080",
                "http://127.0.0.1", "http://localhost",
                "null"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
