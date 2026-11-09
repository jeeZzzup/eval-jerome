package com.eex.application.controller;

import com.eex.application.dto.AccountNameUpdateRequest;
import com.eex.application.dto.AccountResponse;
import com.eex.domain.service.AccountService;
import com.eex.infrastructure.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccountsForClient(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(accountService.getAccountsByClientId(authenticatedUser.clientId())
                .stream().map(AccountResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return accountService.getAccountById(id)
                .filter(account -> account.getClientId().equals(authenticatedUser.clientId()))
                .map(AccountResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByClientId(
            @PathVariable Integer clientId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (!clientId.equals(authenticatedUser.clientId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountService.getAccountsByClientId(clientId)
                .stream().map(AccountResponse::from).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<AccountResponse>> getAccountsByName(
            @RequestParam String name,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(accountService.getAccountsByClientIdAndName(
                        authenticatedUser.clientId(), name)
                .stream().map(AccountResponse::from).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccountName(
            @PathVariable Integer id,
            @RequestBody AccountNameUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return accountService.updateNameForClient(id, authenticatedUser.clientId(), request.name().trim())
                .map(AccountResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
