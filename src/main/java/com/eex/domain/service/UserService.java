package com.eex.domain.service;

import com.eex.domain.entity.User;
import com.eex.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public List<User> getUsersByClientId(Integer clientId) {
        return userRepository.findByClientId(clientId);
    }

    public List<User> getUsersByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getUsersByClientIdAndUsername(Integer clientId, String username) {
        return userRepository.findByClientIdAndUsername(clientId, username);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Integer id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (userDetails.getUsername() != null) {
                user.setUsername(userDetails.getUsername());
            }
            if (userDetails.getPassword() != null) {
                user.setPassword(userDetails.getPassword());
            }
            if (userDetails.getClientId() != null) {
                user.setClientId(userDetails.getClientId());
            }
            return userRepository.save(user);
        }
        throw new RuntimeException("User introuvable avec l'id " + id);
    }

    public void deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User introuvable avec l'id " + id);
        }
    }
}
