package com.yourproject.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại
    private User user;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 50)
    private String transactionType; // Deposit, Withdraw, Transfer...

    @Column(nullable = false)
    private String status; // success, failed, pending

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    private String description;

    // --- Constructors ---
    public Transactions() {}

    public Transactions(User user, Double amount, String transactionType, String status, LocalDateTime transactionDate, String description) {
        this.user = user;
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = status;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    // --- Getters and Setters ---
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
