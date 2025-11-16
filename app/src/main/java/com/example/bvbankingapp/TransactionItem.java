package com.example.bvbankingapp;

public class TransactionItem {
    private String type;
    private double amount;
    private String description;
    private String timestamp;
    private String targetAccount;

    // Full constructor
    public TransactionItem(String type, double amount, String description, String timestamp, String targetAccount) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.targetAccount = targetAccount;
    }

    // Convenience constructor (no targetAccount)
    public TransactionItem(String type, double amount, String description, String timestamp) {
        this(type, amount, description, timestamp, null);
    }

    // Getters & setters...
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getTimestamp() { return timestamp; }
    public String getTargetAccount() { return targetAccount; }

    public void setType(String type) { this.type = type; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setTargetAccount(String targetAccount) { this.targetAccount = targetAccount; }

    @Override
    public String toString() {
        return "TransactionItem{" +
                "type='" + type + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", targetAccount='" + targetAccount + '\'' +
                '}';
    }
}
