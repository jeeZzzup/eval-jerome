package com.eex.application.controller;

import com.eex.application.dto.UserResponse;
import com.eex.domain.service.UserService;
import com.eex.infrastructure.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsersForClient(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(userService.getUsersByClientId(authenticatedUser.clientId())
                .stream().map(UserResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return userService.getUserById(id)
                .filter(user -> user.getClientId().equals(authenticatedUser.clientId()))
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<UserResponse>> getUsersByClientId(
            @PathVariable Integer clientId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (!clientId.equals(authenticatedUser.clientId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userService.getUsersByClientId(clientId)
                .stream().map(UserResponse::from).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> getUsersByUsername(
            @RequestParam String username,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(userService.getUsersByClientIdAndUsername(
                        authenticatedUser.clientId(), username)
                .stream().map(UserResponse::from).toList());
    }
}
