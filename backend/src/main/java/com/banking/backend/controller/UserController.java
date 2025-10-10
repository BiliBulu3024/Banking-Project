package com.banking.backend.controller;


import com.banking.backend.entity.User;
import com.banking.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // endpoint chính
public class UserController {
    @Autowired
    private UserService userService;

    // Đăng ký tài khoản (Create Account)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Nếu role không có, mặc định là USER
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("USER");
            }
            User newUser = userService.createUser(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            // Trả về JSON gọn gàng khi gặp lỗi
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // Lấy tất cả user
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Lấy user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Xóa user theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Đăng nhập (Login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User existingUser = userService.getUserByEmail(user.getEmail());
            if (existingUser != null &&
                    userService.checkPassword(user.getPassword(), existingUser.getPassword())) {

                return ResponseEntity.ok("{\"message\": \"Login successful\", \"role\": \"" + existingUser.getRole() + "\"}");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid email or password\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Something went wrong\"}");
        }
    }


}