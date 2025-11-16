package com.banking.backend.controller;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.User;
import com.banking.backend.repository.UserRepository;
import com.banking.backend.security.JwtUtil;
import com.banking.backend.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AccountService accountService;

    public AccountController(JwtUtil jwtUtil, UserRepository userRepository, AccountService accountService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.accountService = accountService;
    }

//    /** 🧩 Lấy user hiện tại từ JWT */
//    private User getCurrentUser(HttpServletRequest request) {
//        String header = request.getHeader("Authorization");
//        String token = header.substring(7);
//        String username = jwtUtil.getClaims(token).getSubject();
//        return userRepository.findByUsername(username);
//    }
//    /** 🧩 Lấy user hiện tại từ JWT */
    private User getCurrentUser(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        // 🚨 Validate input (CHUẨN FYP, CHUẨN BẢO MẬT)
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = header.substring(7);

        // 🚨 Kiểm tra token có hợp lệ không
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        String username = jwtUtil.getClaims(token).getSubject();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return user;
    }

    // ===========================================================
    // 💰 DEPOSIT (Nạp tiền)
    // ===========================================================
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        double amount = ((Number) body.get("amount")).doubleValue();
        String description = (String) body.getOrDefault("description", "Deposit");

        User user = getCurrentUser(request);
        try {
            if (amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than 0"));
            }

            Account updated = accountService.deposit(user, amount, description);

            System.out.println("💰 [DEPOSIT SUCCESS] User: " + user.getUsername() +
                    " | Amount: " + amount +
                    " | New Balance: " + updated.getBalance());

            return ResponseEntity.ok(Map.of(
                    "message", "Deposit successful",
                    "newBalance", updated.getBalance()
            ));

        } catch (RuntimeException e) {
            System.err.println("⚠️ [DEPOSIT FAILED] User: " + user.getUsername() +
                    " | Reason: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===========================================================
    // 💸 WITHDRAW (Rút tiền)
    // ===========================================================
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        double amount = ((Number) body.get("amount")).doubleValue();
        String description = (String) body.getOrDefault("description", "Withdraw");

        User user = getCurrentUser(request);
        try {
            if (amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than 0"));
            }

            Account updated = accountService.withdraw(user, amount, description);

            System.out.println("💸 [WITHDRAW SUCCESS] User: " + user.getUsername() +
                    " | Amount: " + amount +
                    " | New Balance: " + updated.getBalance());

            return ResponseEntity.ok(Map.of(
                    "message", "Withdraw successful",
                    "newBalance", updated.getBalance()
            ));

        } catch (RuntimeException e) {
            System.err.println("⚠️ [WITHDRAW FAILED] User: " + user.getUsername() +
                    " | Reason: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===========================================================
    // 🔁 TRANSFER (Chuyển tiền)
    // ===========================================================
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        String targetAccount = (String) body.get("targetAccount");
        double amount = Double.parseDouble(body.get("amount").toString());
        User user = getCurrentUser(request);

        try {
            accountService.transfer(user, targetAccount, amount);

            System.out.println("🔁 [TRANSFER SUCCESS] User: " + user.getUsername() +
                    " | Amount: " + amount +
                    " | To: " + targetAccount);

            return ResponseEntity.ok(Map.of(
                    "message", "Transfer successful",
                    "targetAccount", targetAccount
            ));

        } catch (RuntimeException e) {
            System.err.println("⚠️ [TRANSFER FAILED] User: " + user.getUsername() +
                    " | Reason: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===========================================================
    // 📊 Account Info
    // ===========================================================
    @GetMapping("/info")
    public ResponseEntity<?> getAccountInfo(HttpServletRequest request) {
        User user = getCurrentUser(request);
        try {
            Account account = accountService.getAccountByUser(user);
            System.out.println("📊 [ACCOUNT INFO] User: " + user.getUsername() +
                    " | Balance: " + account.getBalance() +
                    " | AccountNumber: " + account.getAccountNumber());

            return ResponseEntity.ok(Map.of(
                    "message", "Account info",
                    "username", user.getUsername(),
                    "accountNumber", account.getAccountNumber(),   // <- BẮT BUỘC
                    "balance", account.getBalance()
            ));
        } catch (RuntimeException e) {
            System.err.println("⚠️ [ACCOUNT INFO FAILED] User: " + user.getUsername() +
                    " | Reason: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserInfo(HttpServletRequest request) {
        User user = getCurrentUser(request);

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt(),
                "status", user.getStatus()
        ));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {

        User user = getCurrentUser(request);

        String email = (String) body.get("email");
        String password = (String) body.get("password");

        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }

        if (password != null && !password.isBlank()) {
            user.setPassword(new BCryptPasswordEncoder().encode(password));
        }

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "email", user.getEmail()
        ));
    }
    @GetMapping("/lookup/{acc}")
    public ResponseEntity<?> lookupAccount(@PathVariable String acc) {
        Account account = accountService.findByAccountNumber(acc);
        if (account == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Account not found"));
        }
        User user = account.getUser();
        return ResponseEntity.ok(Map.of(
                "accountNumber", acc,
                "owner", user.getUsername()
        ));
    }

}