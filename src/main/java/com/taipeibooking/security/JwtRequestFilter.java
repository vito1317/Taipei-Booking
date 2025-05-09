package com.taipeibooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    private final RequestMatcher publicEndpoints;

    @Autowired
    public JwtRequestFilter(JwtTokenProvider tokenProvider, @Lazy UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;

        List<RequestMatcher> matchers = Arrays.asList(
                new AntPathRequestMatcher("/api/user/login", "POST"),
                new AntPathRequestMatcher("/api/user/register", "POST"),
                new AntPathRequestMatcher("/api/attractions", "GET"),
                new AntPathRequestMatcher("/api/attractions/**", "GET"),
                new AntPathRequestMatcher("/api/districts", "GET"),
                new AntPathRequestMatcher("/static/**"),
                new AntPathRequestMatcher("/"),
                new AntPathRequestMatcher("/attraction/*"),
                new AntPathRequestMatcher("/booking"),
                new AntPathRequestMatcher("/thankyou"),
                new AntPathRequestMatcher("/panel/**"),
                new AntPathRequestMatcher("/favicon.ico")
        );
        this.publicEndpoints = new OrRequestMatcher(matchers);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (publicEndpoints.matches(request)) {
            logger.trace("請求 {} 匹配公開端點，跳過 JWT 驗證。", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        logger.trace("處理請求 {} 的 JWT 驗證。", request.getRequestURI());
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String usernameOrEmail = tokenProvider.getUsernameFromJWT(jwt);
                logger.debug("從 JWT 獲取到 Username/Email: {}", usernameOrEmail);

                UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("已為使用者 '{}' 設定 Security Context", usernameOrEmail);
                } else {
                     logger.warn("無法通過 username/email '{}' 加載 UserDetails", usernameOrEmail);
                }

            } else {
                 if (StringUtils.hasText(jwt)) {
                     logger.debug("JWT Token 無效或已過期: {}", jwt);
                 } else {
                     logger.trace("請求 {} 沒有有效的 JWT Token。", request.getRequestURI());
                 }
            }
        } catch (Exception ex) {
            logger.error("在 JWT 過濾器中設置用戶認證時出錯: {}", ex.getMessage(), ex);
             SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        logger.trace("請求 {} 的 Header 中未找到 Bearer Token。", request.getRequestURI());
        return null;
    }
}
