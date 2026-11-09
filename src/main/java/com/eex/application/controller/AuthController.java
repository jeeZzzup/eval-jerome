package com.eex.application.controller;

import com.eex.application.dto.AuthResponse;
import com.eex.application.dto.LoginRequest;
import com.eex.domain.entity.User;
import com.eex.domain.service.UserService;
import com.eex.domain.util.JwtUtil;
import com.eex.domain.util.PasswordUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        if (loginRequest.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Requête invalide"));
        }
        List<User> users = userService.getUsersByUsername(loginRequest.getUsername());
        if (users.isEmpty() || !PasswordUtil.matches(loginRequest.getPassword(), users.get(0).getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Identifiants invalides"));
        }

        String token = jwtUtil.generateToken(users.get(0).getId());
        return ResponseEntity.ok(new AuthResponse(token, "Authentification réussie"));
    }

    @PostMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized("Jeton manquant ou invalide");
        }

        String token = authHeader.substring(7).trim();
        Integer userId = jwtUtil.validateAndGetUserId(token);
        if (userId == null || userService.getUserById(userId).isEmpty()) {
            return unauthorized("Jeton invalide ou expiré");
        }

        return ResponseEntity.ok(new AuthResponse(token, "Jeton valide"));
    }

    private ResponseEntity<AuthResponse> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, message));
    }
}
