package com.example.bankaccount;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public abstract class Customer extends com.example.bankaccount.User {
    private int customerId;
    private int userId;
    protected String address;
    protected String email;
    protected String phoneNumber;
    protected Date registrationDate;
    protected List<Account> accounts;
    protected String customerType;

    public Customer(String username, String password, String address, String email, String phoneNumber, String customerType) {
        super(username, password, "Customer");
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.registrationDate = new Date();
        this.accounts = new ArrayList<>();
        this.customerType = customerType;
    }

    // Getters
    public int getCustomerId() {return customerId;}
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Date getRegistrationDate() { return registrationDate; }
    public List<Account> getAccounts() { return accounts; }
    public String getCustomerType() { return customerType; }
    public int getUserId() { return userId; }

    //Setters
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setAddress(String address) { this.address = address; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }
    public void setAccounts(List<com.example.bankaccount.Account> accounts) { this.accounts = accounts; }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public abstract String getDisplayName();
    public abstract String getIdentificationNumber();
    public abstract boolean isValidCustomer();

}