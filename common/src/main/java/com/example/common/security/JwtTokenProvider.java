package com.example.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long expirationMillis;
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    public String generateToken(String userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 驗證 Token 是否有效
     */
    public boolean isValidToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token is expired:{}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 從 Token 中解析 Claims
     */
    public Claims getClaims(String token) {
        return parseClaims(token);
    }

    /**
     * 從 Token 中取得用戶 ID
     */
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }
    public String getUsernameFromToken(String token) {
        return parseClaims(token).get("username", String.class);
    }
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }
    public String getJtiFromToken(String token) {
        return parseClaims(token).getId();
    }
    /**
     * 取得 Token 剩餘有效時間 (秒)
     */
    public long getExpirationFromToken(String token) {
        Claims claims = parseClaims(token);
        return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
    }

    /**
     * 解析 Token，回傳 Claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
