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

    public Customer getCustomer() {
        return customer;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited " + amount + " into account " + accountNumber);
            Transaction transaction = new Transaction(accountNumber, "DEPOSIT", amount, balance, "Cash deposit");
            transactionHistory.add(transaction);
        } else {
            System.out.println("Deposit must be positive.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            System.out.println("Withdrew " + amount + " from account " + accountNumber);
        } else {
            System.out.println("Not enough balance or invalid amount.");
        }
    }

    public double getBalance() {
        return balance;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public ArrayList<Transaction> getTransactionHistory() {
        return transactionHistory;
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