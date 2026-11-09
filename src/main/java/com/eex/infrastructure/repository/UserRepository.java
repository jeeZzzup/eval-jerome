package com.eex.infrastructure.repository;

import com.eex.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByClientId(Integer clientId);
    List<User> findByUsername(String username);
    List<User> findByClientIdAndUsername(Integer clientId, String username);
}
