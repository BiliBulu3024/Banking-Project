package com.banking.backend;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.User;
import com.banking.backend.repository.AccountRepository;
import com.banking.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   AccountRepository accountRepository,
                                   BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            String defaultUsername = "BiliBuluTest";
            String defaultEmail = "Bilibulu@example.com";

            // 🔹 B1: Kiểm tra user mặc định đã tồn tại chưa
            User user = userRepository.findByUsername(defaultUsername);
            if (user == null) {
                user = new User();
                user.setUsername(defaultUsername);
                user.setEmail(defaultEmail);
                user.setPassword(passwordEncoder.encode("123456")); // ✅ Mã hóa password
                user.setRole("USER");
                userRepository.save(user);
                System.out.println("✅ Created default user: " + defaultUsername);
            } else {
                System.out.println("ℹ️ Default user already exists: " + defaultUsername);
            }

            // 🔹 B2: Nếu user chưa có account → tạo account mặc định
            if (accountRepository.findByUser(user).isEmpty()) {
                Account acc = new Account();
                acc.setUser(user);
                acc.setAccountNumber("ACC" + System.currentTimeMillis()); // sinh số tài khoản duy nhất
                acc.setBalance(0.0);
                accountRepository.save(acc);
                System.out.println("💳 Created default account for " + defaultUsername);
            } else {
                System.out.println("ℹ️ Account already exists for " + defaultUsername);
            }
        };
    }
}
