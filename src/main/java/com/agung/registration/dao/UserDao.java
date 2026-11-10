package com.agung.registration.dao;

import com.agung.registration.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
    List<User> findAll();
}
