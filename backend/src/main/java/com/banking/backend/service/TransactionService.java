package com.banking.backend.service;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.Transaction;
import com.banking.backend.entity.User;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    List<Transaction> getTransactionsByAccount(Account account);
    List<Transaction> filterTransactions(Account account, String type, String start, String end);
    User findUserByUsername(String username);

    // ✅ Thêm mới
    Map<String, Object> getTransactionSummary(Account account);

}
