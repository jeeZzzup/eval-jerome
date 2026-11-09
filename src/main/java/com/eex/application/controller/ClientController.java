package com.eex.application.controller;

import com.eex.domain.entity.Client;
import com.eex.domain.service.ClientService;
import com.eex.infrastructure.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<Client>> getClient(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return clientService.getClientById(authenticatedUser.clientId())
                .map(client -> ResponseEntity.ok(List.of(client)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<Client> getMyClient(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return clientService.getClientById(authenticatedUser.clientId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (!id.equals(authenticatedUser.clientId())) {
            return ResponseEntity.notFound().build();
        }
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
