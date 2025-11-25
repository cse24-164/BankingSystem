package com.example.bankaccount;

import java.util.ArrayList;
import java.util.Date;

public abstract class Account {
    private String accountNumber;
    protected double balance;
    private String branch;
    private com.example.bankaccount.Customer customer;
    private ArrayList<Transaction> transactionHistory;
    protected String accountType;

    public Account(String branch, com.example.bankaccount.Customer customer, String accountType) {
        this.branch = branch;
        this.customer = customer;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
        this.accountType = accountType;
    }

    //Getters
    public String getAccountNumber() {
        return accountNumber;
    }
    public double getBalance() {
        return balance;
    }
    public String getBranch() {
        return branch;
    }
    public com.example.bankaccount.Customer getCustomer() {return customer;}
    public ArrayList<com.example.bankaccount.Transaction> getTransactionHistory() {
        return transactionHistory;
    }
    public String getAccountType() {return accountType;}

    //Setters
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void deposit(double amount) {
        if (amount >= 0) {
            balance += amount;
            Transaction transaction = new Transaction(accountNumber, "DEPOSIT", amount, balance, "Cash deposit");
            transactionHistory.add(transaction);
        }
    }

    protected void updateBalance(double newBalance) {
        this.balance = newBalance;
    }

    public abstract void showAccountType();


}