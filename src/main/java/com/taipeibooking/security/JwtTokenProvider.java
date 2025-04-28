package com.taipeibooking.security; 

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    
    @Value("${jwt.secret}")
    private String jwtSecretString;

    
    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    
    private SecretKey jwtSecretKey;

    
    @PostConstruct
    public void init() {
        
        
        
        try {
            
            
            if (jwtSecretString.getBytes().length < 32) {
                logger.warn("警告：JWT 密鑰 '{}' 的長度不足 32 位元組 (256 位元)，建議使用更長的密鑰。", jwtSecretString);
                
                
            }
            this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
        } catch (Exception e) {
            logger.error("初始化 JWT 密鑰失敗: {}", e.getMessage(), e);
            
            throw new RuntimeException("無法初始化 JWT 密鑰", e);
        }
    }

    
    public String generateToken(Authentication authentication) {
        
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String username = userPrincipal.getUsername();

        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        
        return Jwts.builder()
                .setSubject(username) 
                .setIssuedAt(now)      
                .setExpiration(expiryDate) 
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512) 
                .compact(); 
    }

     
    public String getUsernameFromJWT(String token) {
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey) 
                .build()
                .parseClaimsJws(token) 
                .getBody(); 

        
        return claims.getSubject();
    }

    
    public boolean validateToken(String authToken) {
        try {
            
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("無效的 JWT 簽名: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("無效的 JWT Token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("JWT Token 已過期: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("不支援的 JWT Token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT Claims 字串為空: {}", ex.getMessage());
        }
        return false; 
    }
}