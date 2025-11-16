package com.banking.backend.service;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.Transaction;
import com.banking.backend.entity.User;
import com.banking.backend.repository.TransactionRepository;
import com.banking.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;

    public TransactionServiceImpl(TransactionRepository transactionRepo, UserRepository userRepo) {
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Transaction> getTransactionsByAccount(Account account) {
        return transactionRepo.findByAccountId(account.getId());
    }

    @Override
    public List<Transaction> filterTransactions(Account account, String type, String start, String end) {
        List<Transaction> transactions = transactionRepo.findByAccountId(account.getId());

        // ✅ Lọc theo loại giao dịch
        if (type != null && !type.isEmpty()) {
            transactions = transactions.stream()
                    .filter(tx -> tx.getType().equalsIgnoreCase(type))
                    .toList();
        }

        // ✅ Lọc theo ngày (nếu có start + end)
        if (start != null && end != null) {
            LocalDateTime startDate = LocalDateTime.parse(start + "T00:00:00");
            LocalDateTime endDate = LocalDateTime.parse(end + "T23:59:59");

            transactions = transactions.stream()
                    .filter(tx -> !tx.getTimestamp().isBefore(startDate) && !tx.getTimestamp().isAfter(endDate))
                    .toList();
        }

        return transactions;
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    /** 📊 Tính toán tổng hợp giao dịch */
    @Override
    public Map<String, Object> getTransactionSummary(Account account) {
        List<Transaction> transactions = transactionRepo.findByAccountId(account.getId());

        double totalDeposit = 0;
        double totalWithdraw = 0;
        double totalTransferOut = 0;
        double totalTransferIn = 0;

        for (Transaction tx : transactions) {
            switch (tx.getType()) {
                case "DEPOSIT" -> totalDeposit += tx.getAmount();
                case "WITHDRAW" -> totalWithdraw += tx.getAmount();
                case "TRANSFER_OUT" -> totalTransferOut += tx.getAmount();
                case "TRANSFER_IN" -> totalTransferIn += tx.getAmount();
            }
        }

        double netChange = totalDeposit + totalTransferIn - totalWithdraw - totalTransferOut;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDeposit", totalDeposit);
        summary.put("totalWithdraw", totalWithdraw);
        summary.put("totalTransferOut", totalTransferOut);
        summary.put("totalTransferIn", totalTransferIn);
        summary.put("netChange", netChange);
        summary.put("currentBalance", account.getBalance());

        return summary;
    }
}
