package com.example.bankaccount;

public class AuthContext {

    private String username;
    private String userType; // "CUSTOMER" or "BANK_TELLER"
    private Object userObject; // Customer or BankTeller instance

    public AuthContext(String username, String userType, Object userObject) {
        this.username = username;
        this.userType = userType;
        this.userObject = userObject;
    }

    // Getters
    public String getUsername() { return username; }
    public String getUserType() { return userType; }

    public boolean isCustomer() {
        return "CUSTOMER".equals(userType);
    }

    public boolean isBankTeller() {
        return "BANK_TELLER".equals(userType); }

    // Type-safe getters
    public Customer getCustomer() {
        return isCustomer() ? (Customer) userObject : null;
    }

    public BankTeller getBankTeller() {
        return isBankTeller() ? (BankTeller) userObject : null;
    }
}

