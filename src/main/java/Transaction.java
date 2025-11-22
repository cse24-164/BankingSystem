package com.example.bankaccount;
import java.util.Date;

public class Transaction {
    private static int nextTransactionId = 1000;

    private int transactionId;
    private String accountNumber;
    private String transactionType;
    private double amount;
    private double balance;
    private Date transactionDate;
    private String description;

    public Transaction(String accountNumber, String transactionType, double amount,
                       double balance, String description) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balance = balance;
        this.description = description;
        this.transactionId = nextTransactionId++;
        this.transactionDate = new Date();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalance() {
        return balance;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}
