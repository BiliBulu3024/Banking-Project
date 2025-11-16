package com.banking.backend.service;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.Transaction;
import com.banking.backend.entity.User;
import com.banking.backend.repository.AccountRepository;
import com.banking.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    public AccountServiceImpl(AccountRepository accountRepo, TransactionRepository transactionRepo) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
    }

    // ==============================
    // 🔹 Lấy tài khoản theo User
    // ==============================
    @Override
    public Account getAccountByUser(User user) {
        return accountRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Account not found for user: " + user.getUsername()));
    }

    // ==============================
    // 🔹 Lookup theo accountNumber (PHỤC VỤ TRANSFER)
    // ==============================
    @Override
    public Account findByAccountNumber(String accountNumber) {
        return accountRepo.findByAccountNumber(accountNumber);
    }

    // ==============================
    // 🔹 Nạp tiền
    // ==============================
    @Override
    @Transactional
    public Account deposit(User user, double amount, String description) {
        Account acc = getAccountByUser(user);

        if (amount <= 0)
            throw new RuntimeException("Deposit amount must be positive");

        acc.setBalance(acc.getBalance() + amount);
        accountRepo.save(acc);

        Transaction tx = Transaction.builder()
                .account(acc)
                .type("DEPOSIT")
                .amount(amount)
                .description(description != null ? description : "Deposit funds")
                .build();

        transactionRepo.save(tx);
        return acc;
    }

    // ==============================
    // 🔹 Rút tiền
    // ==============================
    @Override
    @Transactional
    public Account withdraw(User user, double amount, String description) {
        Account acc = getAccountByUser(user);

        if (amount <= 0)
            throw new RuntimeException("Withdraw amount must be positive");
        if (acc.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        acc.setBalance(acc.getBalance() - amount);
        accountRepo.save(acc);

        Transaction tx = Transaction.builder()
                .account(acc)
                .type("WITHDRAW")
                .amount(amount)
                .description(description != null ? description : "Withdraw funds")
                .build();

        transactionRepo.save(tx);
        return acc;
    }

    // ==============================
    // 🔹 Chuyển khoản
    // ==============================
    @Override
    @Transactional
    public void transfer(User user, String targetAccountNumber, double amount) {
        Account sender = getAccountByUser(user);
        Account receiver = accountRepo.findByAccountNumber(targetAccountNumber);

        if (receiver == null)
            throw new RuntimeException("Target account not found");
        if (sender.getAccountNumber().equals(targetAccountNumber))
            throw new RuntimeException("Cannot transfer to your own account");
        if (amount <= 0)
            throw new RuntimeException("Transfer amount must be positive");
        if (sender.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        // Cập nhật số dư
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);
        accountRepo.save(sender);
        accountRepo.save(receiver);

        // Log giao dịch người gửi
        transactionRepo.save(Transaction.builder()
                .account(sender)
                .type("TRANSFER_OUT")
                .amount(amount)
                .targetAccount(receiver.getAccountNumber())
                .description("Transfer to " + receiver.getAccountNumber())
                .build());

        // Log giao dịch người nhận
        transactionRepo.save(Transaction.builder()
                .account(receiver)
                .type("TRANSFER_IN")
                .amount(amount)
                .targetAccount(sender.getAccountNumber())
                .description("Received from " + sender.getAccountNumber())
                .build());
    }

    // ==============================
    // 🔹 Lấy lịch sử giao dịch
    // ==============================
    @Override
    public List<Transaction> getTransactions(String accountNumber) {
        Account acc = accountRepo.findByAccountNumber(accountNumber);
        if (acc == null) throw new RuntimeException("Account not found");
        return transactionRepo.findByAccountId(acc.getId());
    }
}
