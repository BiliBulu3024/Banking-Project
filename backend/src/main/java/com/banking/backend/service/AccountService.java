package com.banking.backend.service;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.Transaction;
import com.banking.backend.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface AccountService {
    Account deposit(User user, double amount, String description);
    Account withdraw(User user, double amount, String description);
    void transfer(User user, String targetAccount, double amount);


    List<Transaction> getTransactions(String accountNumber);
    Account getAccountByUser(User user);

    Account findByAccountNumber(String acc);
}