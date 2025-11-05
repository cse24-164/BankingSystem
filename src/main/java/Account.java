package com.example.bankaccount;

import java.util.ArrayList;

public abstract class Account {
    private static int nextAccountNumber = 1000;

    private int accountNumber;
    private double balance;
    private String branch;
    private Customer customer;
    private ArrayList<Transaction> transactionHistory;

    public Account(String branch, Customer customer) {
        this.accountNumber = ++nextAccountNumber;
        this.branch = branch;
        this.balance = 0.0;
        this.customer = customer;
        this.transactionHistory = new ArrayList<>();
    }

    //Getters
    public int getAccountNumber() {
        return accountNumber;
    }
    public double getBalance() {
        return balance;
    }
    public String getBranch() {
        return branch;
    }
    public Customer getCustomer() {return customer;}
    public ArrayList<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    //Setters
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            Transaction transaction = new Transaction(accountNumber, "DEPOSIT", amount, balance, "Cash deposit");
            transactionHistory.add(transaction);
        }
    }

    protected void updateBalance(double newBalance) {
        this.balance = newBalance;
    }

    protected void addInterestTransaction(double interestAmount, String description) {
        balance += interestAmount;
        Transaction transaction = new Transaction(accountNumber, "INTEREST", interestAmount,
                balance, description);
        transactionHistory.add(transaction);
    }

    public abstract void showAccountType();

}