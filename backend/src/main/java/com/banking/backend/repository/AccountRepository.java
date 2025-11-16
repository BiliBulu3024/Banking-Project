package com.banking.backend.repository;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountNumber(String accountNumber);
    Optional<Account> findByUser(User user);
}