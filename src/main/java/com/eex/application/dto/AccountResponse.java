package com.eex.application.dto;

import com.eex.domain.entity.Account;

public record AccountResponse(Integer id, String name, Double amount, Integer clientId) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(), account.getName(), account.getAmount(), account.getClientId());
    }
}
