package com.banking.backend.controller;

import com.banking.backend.entity.Account;
import com.banking.backend.entity.Transaction;
import com.banking.backend.entity.User;
import com.banking.backend.security.JwtUtil;
import com.banking.backend.service.AccountService;
import com.banking.backend.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final JwtUtil jwtUtil;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public TransactionController(JwtUtil jwtUtil, AccountService accountService, TransactionService transactionService) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String username = jwtUtil.getClaims(token).getSubject();
        return transactionService.findUserByUsername(username);
    }

    /** 📜 Lấy lịch sử giao dịch */
    @GetMapping
    public ResponseEntity<?> getTransactions(
            HttpServletRequest request,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        try {
            User user = getCurrentUser(request);
            Account account = accountService.getAccountByUser(user);

            List<Transaction> transactions = transactionService.filterTransactions(account, type, start, end);

            return ResponseEntity.ok(Map.of(
                    "message", "Transaction history retrieved successfully",
                    "total", transactions.size(),
                    "transactions", transactions
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /** 📊 Lấy thống kê tổng quan giao dịch */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            Account account = accountService.getAccountByUser(user);

            Map<String, Object> summary = transactionService.getTransactionSummary(account);

            return ResponseEntity.ok(Map.of(
                    "message", "Transaction summary generated successfully",
                    "accountNumber", account.getAccountNumber(),
                    "summary", summary
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** 📊 Dữ liệu biểu đồ giao dịch (Pie + Line Chart) */
    @GetMapping("/summary/chart")
    public ResponseEntity<?> getChartSummary(HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            Account account = accountService.getAccountByUser(user);
            List<Transaction> transactions = transactionService.getTransactionsByAccount(account);

            double totalDeposit = 0;
            double totalWithdraw = 0;
            double totalTransfer = 0;

            Map<String, Double> dailyChanges = new HashMap<>();

            for (Transaction tx : transactions) {
                String date = tx.getTimestamp().toLocalDate().toString(); // YYYY-MM-DD

                switch (tx.getType()) {
                    case "DEPOSIT" -> totalDeposit += tx.getAmount();
                    case "WITHDRAW" -> totalWithdraw += tx.getAmount();
                    case "TRANSFER_IN", "TRANSFER_OUT" -> totalTransfer += tx.getAmount();
                }

                // ✅ Cộng dồn biến động theo ngày
                dailyChanges.put(date,
                        dailyChanges.getOrDefault(date, 0.0)
                                + (tx.getType().equals("WITHDRAW") || tx.getType().equals("TRANSFER_OUT")
                                ? -tx.getAmount() : tx.getAmount()));
            }

            double totalAll = totalDeposit + totalWithdraw + totalTransfer;
            double depositPercent = totalAll > 0 ? (totalDeposit / totalAll) * 100 : 0;
            double withdrawPercent = totalAll > 0 ? (totalWithdraw / totalAll) * 100 : 0;
            double transferPercent = totalAll > 0 ? (totalTransfer / totalAll) * 100 : 0;

            Map<String, Object> chartData = Map.of(
                    "pieChart", Map.of(
                            "deposit", depositPercent,
                            "withdraw", withdrawPercent,
                            "transfer", transferPercent
                    ),
                    "lineChart", dailyChanges
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Chart summary generated successfully",
                    "accountNumber", account.getAccountNumber(),
                    "chartData", chartData
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
