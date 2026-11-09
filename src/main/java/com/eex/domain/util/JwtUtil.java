package com.eex.domain.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:AZERTYUIOP0123456789AZERTYUIOP0123456789AZERTYUIOP0123456789}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String generateToken(Integer userId) {
        try {
            String token = Jwts.builder()
                    .subject(userId.toString())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .compact();

            logger.info("Jeton généré avec succès pour l'utilisateur : " + userId);
            return token;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du jeton : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du jeton", e);
        }
    }

    public Integer validateAndGetUserId(String token) {
        try {
            String userId = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            logger.info("Jeton validé avec succès pour l'utilisateur : " + userId);
            return Integer.parseInt(userId);
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du jeton : " + e.getMessage());
            return null;
        }
    }
}

