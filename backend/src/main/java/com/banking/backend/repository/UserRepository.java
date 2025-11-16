package com.banking.backend.repository;

import com.banking.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
    public interface UserRepository extends JpaRepository<User, Long> {

        // Tìm user bằng email
        User findByEmail(String email);

        // Tìm user bằng username
        User findByUsername(String username);
        Optional<User> findByUsernameOrEmail(String username, String email);

    }
