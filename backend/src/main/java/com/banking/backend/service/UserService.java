package com.banking.backend.service;

import com.banking.backend.entity.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    List<User> getAllUsers();
    User getUserByEmail(String email);
    void deleteUser(Long id);
    boolean checkPassword(String rawPassword, String encodedPassword);
    User updateUser(Long id, User updatedUser);
}
