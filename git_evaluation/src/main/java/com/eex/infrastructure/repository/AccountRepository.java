package com.eex.infrastructure.repository;

import com.eex.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByClientId(Integer clientId);
    List<Account> findByName(String name);
}

