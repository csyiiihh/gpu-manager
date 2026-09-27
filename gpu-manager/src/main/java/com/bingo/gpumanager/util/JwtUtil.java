package com.bingo.gpumanager.util;

import com.bingo.gpumanager.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireTime;

    public JwtUtil(JwtProperties properties) {

        this.key = Keys.hmacShaKeyFor(
                properties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );

        this.expireTime = properties.getExpireTime();
    }

    public String generateToken(
            Long userId,
            String username,
            String role) {

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expireTime
                        )
                )
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {

        Claims claims = parseToken(token);

        return claims.get("userId", Long.class);
    }

    public String getUsername(String token) {

        Claims claims = parseToken(token);

        return claims.getSubject();
    }
}