package com.agung.registration.service;

import com.agung.registration.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    boolean isUsernameTaken(String username);
    boolean isEmailTaken(String email);
    List<User> getAllUsers();
}
