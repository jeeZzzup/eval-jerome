package com.eex.application.controller;

import com.eex.application.dto.AuthResponse;
import com.eex.application.dto.LoginRequest;
import com.eex.domain.entity.User;
import com.eex.domain.service.UserService;
import com.eex.domain.util.JwtUtil;
import com.eex.domain.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Tentative de connexion pour l'utilisateur : " + loginRequest.getUsername());

        List<User> users = userService.getUsersByUsername(loginRequest.getUsername());

        if (users.isEmpty()) {
            logger.warn("Utilisateur non trouvé : " + loginRequest.getUsername());
            AuthResponse response = new AuthResponse(null, "Identifiants invalides");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        User user = users.get(0);

        if (!PasswordUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Mot de passe incorrect pour l'utilisateur : " + loginRequest.getUsername());
            AuthResponse response = new AuthResponse(null, "Identifiants invalides");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getId());
        logger.info("Connexion réussie pour l'utilisateur : " + loginRequest.getUsername());

        AuthResponse response = new AuthResponse(token, "Authentification réussie");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("Validation du jeton");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("En-tête Authorization manquant ou invalide");
            AuthResponse response = new AuthResponse(null, "Jeton manquant ou invalide");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        Integer userId = jwtUtil.validateAndGetUserId(token);

        if (userId == null) {
            logger.warn("Jeton invalide ou expiré");
            AuthResponse response = new AuthResponse(null, "Jeton invalide ou expiré");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        logger.info("Jeton valide pour l'utilisateur : " + userId);
        AuthResponse response = new AuthResponse(token, "Jeton valide pour l'utilisateur : " + userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

