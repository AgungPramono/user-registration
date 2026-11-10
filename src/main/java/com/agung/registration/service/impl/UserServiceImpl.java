package com.agung.registration.service.impl;

import com.agung.registration.dao.UserDao;
import com.agung.registration.exception.DuplicateResourceException;
import com.agung.registration.exception.ValidationException;
import com.agung.registration.model.User;
import com.agung.registration.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (user == null) {
            throw new ValidationException("User object cannot be null");
        }
        if (userDao.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username '" + user.getUsername() + "' is already taken");
        }
        if (userDao.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email '" + user.getEmail() + "' is already registered");
        }
        return userDao.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (id == null) {
            throw new ValidationException("User ID cannot be null");
        }
        return userDao.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username cannot be empty");
        }
        return userDao.findByUsername(username.trim());
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }
        return userDao.findByEmail(email.trim().toLowerCase());
    }

    @Override
    public boolean isUsernameTaken(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return userDao.existsByUsername(username.trim());
    }

    @Override
    public boolean isEmailTaken(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return userDao.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }
}
