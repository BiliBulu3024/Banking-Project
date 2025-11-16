package com.banking.backend.controller;

import com.banking.backend.entity.User;
import com.banking.backend.security.JwtUtil;
import com.banking.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    // === Login ===
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String input = body.get("username"); // username or email
        String password = body.get("password");

        User user = userService.getAllUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(input) || u.getEmail().equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            //  Tạo JWT token
            String token = jwtUtil.generateToken(user.getUsername(), Map.of("role", user.getRole()));

            // Trả về JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("token", token);

            return ResponseEntity.ok(response);
        }

        //  Sai thông tin đăng nhập
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username/email or password"));
    }
}
