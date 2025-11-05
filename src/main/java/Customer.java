package com.example.bankaccount;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public abstract class Customer extends User {
    protected String address;
    protected String email;
    protected String phoneNumber;
    protected Date registrationDate;
    protected List<Account> accounts;

    public Customer(String username, String password, String address, String email, String phoneNumber) {
        super(username, password, "Customer");
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.registrationDate = new Date();
        this.accounts = new ArrayList<>();
    }

    // Getters
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Date getRegistrationDate() { return registrationDate; }
    public List<Account> getAccounts() { return accounts; }

    //Setters
    public void setAddress(String address) { this.address = address; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public abstract String getDisplayName();
    public abstract String getIdentificationNumber();
    public abstract boolean isValidCustomer();

}