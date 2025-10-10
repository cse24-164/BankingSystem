package com.example.bankaccount;

import java.util.ArrayList;

public class Customer {
    private String firstName;
    private String surname;
    private String address;
    private String pin; // new: pin for login
    private ArrayList<Account> accounts;

    public Customer(String firstName, String surname, String address, String pin) {
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.pin = pin;
        this.accounts = new ArrayList<>();
    }

    public String getPin() {
        return pin;
    }

    public void openAccount(Account account) {
        accounts.add(account);
        System.out.println("New account opened for " + firstName + " " + surname);
    }

    public void depositToAccount(int accountIndex, double amount) {
        if (accountIndex >= 0 && accountIndex < accounts.size()) {
            accounts.get(accountIndex).deposit(amount);
        } else {
            System.out.println("Invalid account index.");
        }
    }

    public void showAccounts() {
        for (int i = 0; i < accounts.size(); i++) {
            System.out.print("[" + i + "] ");
            accounts.get(i).showAccountType();
            System.out.println("Balance: " + accounts.get(i).getBalance());
        }
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }
}
