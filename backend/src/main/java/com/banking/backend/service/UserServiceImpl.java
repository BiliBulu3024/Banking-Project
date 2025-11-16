package com.banking.backend.service;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.User;
import com.banking.backend.repository.AccountRepository;
import com.banking.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           AccountRepository accountRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public User createUser(User user) {
        // ✅ Kiểm tra trùng email / username
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists!");
        }
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists!");
        }

        // ✅ Gán role mặc định nếu chưa có
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        // ✅ Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ✅ Bước 1: Lưu user vào database
        User savedUser = userRepository.save(user);

        // ✅ Bước 2: Tạo account mặc định cho user này
        Account account = new Account();
        account.setUser(savedUser);
        account.setAccountNumber("ACC" + System.currentTimeMillis()); // số tài khoản duy nhất
        account.setBalance(0.0);
        accountRepository.save(account);

        System.out.println("💳 Created account for user: " + savedUser.getUsername()
                + " | Account Number: " + account.getAccountNumber());

        return savedUser;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found to delete");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        if (updatedUser.getUsername() != null)
            existingUser.setUsername(updatedUser.getUsername());

        if (updatedUser.getEmail() != null)
            existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty())
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));

        if (updatedUser.getRole() != null)
            existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }
}
