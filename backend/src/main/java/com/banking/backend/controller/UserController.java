package com.banking.backend.controller;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.User;
import com.banking.backend.security.JwtUtil;
import com.banking.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.banking.backend.repository.AccountRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AccountRepository accountRepository;

    // ✅ Constructor-based injection (chuẩn FYP)
    public UserController(UserService userService, AccountRepository accountRepository) {
        this.userService = userService;
        this.accountRepository = accountRepository;
    }

    /** 🧾 Register User + Auto Create Account */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        try {
            // ✅ Gọi service tạo user (đã bao gồm tạo account)
            User newUser = userService.createUser(
                    new User(null, username, email, password, "USER")
            );

            // ✅ Lấy account của user mới
            Account account = accountRepository.findByUser(newUser)
                    .orElseThrow(() -> new RuntimeException("Account not found for new user"));

            // ✅ Trả về response đẹp, chuẩn format
            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "username", newUser.getUsername(),
                    "email", newUser.getEmail(),
                    "role", newUser.getRole(),
                    "accountNumber", account.getAccountNumber(),
                    "balance", account.getBalance()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // === 2. Get All Users ===
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // === 3. Get User by ID ===
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // === 4. Delete User ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }


}
