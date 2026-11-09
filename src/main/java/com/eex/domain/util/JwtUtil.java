package com.eex.domain.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey signingKey;
    private final long jwtExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration:86400000}") long jwtExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(Integer userId) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + jwtExpiration))
                .signWith(signingKey)
                .compact();
    }

    public Integer validateAndGetUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String userId = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            int parsedUserId = Integer.parseInt(userId);
            return parsedUserId > 0 ? parsedUserId : null;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
