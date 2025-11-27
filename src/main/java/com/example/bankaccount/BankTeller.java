package com.example.bankaccount;

import java.util.Collections;
import java.util.List;

public class BankTeller extends User {
    private String tellerId;
    private String firstName;
    private String lastName;
    private String branchCode;
    private BankingService bankingService;

    public BankTeller(String username, String password, String firstName, String lastName) {
        super(username, password, "BankTeller");
        this.firstName = firstName;
        this.lastName = lastName;
        this.bankingService = new BankingService();
    }

    public BankTeller(String username, String password, String userType, String tellerId, String firstName,
                      String lastName, String branchCode, BankingService bankingService) {
        super(username, password, userType);
        this.tellerId = tellerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.branchCode = branchCode;
        this.bankingService = bankingService;
    }


    // Getters
    public String getTellerId() { return tellerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getBranchCode() { return branchCode; }
    public BankingService getBankingService() { return bankingService; }


    public Account openAccount(Customer customer, String accountType, String branch, double initialDeposit) {
        if (customer == null) {
            System.err.println("Customer cannot be null");
            return null;
        }

        if (accountType == null || accountType.isBlank()) {
            System.err.println("Account type is not specified");
            return null;
        }

        accountType = accountType.toLowerCase().trim();
        Account newAccount;

        switch (accountType) {
            case "cheque" -> newAccount = new ChequeAccount(branch, customer, initialDeposit);
            case "savings" -> newAccount = new SavingsAccount(branch, customer, initialDeposit);
            case "investment" -> newAccount = new InvestmentAccount(branch, customer, initialDeposit);
            default -> {
                System.err.println("Unknown account type: " + accountType);
                return null;
            }
        }

        customer.addAccount(newAccount);

        System.out.println("Bank Teller " + getFullName() +
                " successfully opened " + accountType +
                " account (#" + newAccount.getAccountNumber() + ") for " +
                customer.getDisplayName() + " with initial deposit: P" + initialDeposit);

        return newAccount;
    }

    public void processDeposit(String accountNumber, double amount, String description) {
        try {
            bankingService.deposit(accountNumber, amount, description);
            System.out.println("Teller " + getFullName() + " processed deposit: P" + amount +
                    " to account #" + accountNumber);
        } catch (Exception e) {
            System.err.println("Deposit failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public double checkBalance(String accountNumber) {
        try {
            double balance = bankingService.getAccountBalance(accountNumber);
            System.out.println("Teller " + getFullName() + " checked balance for account #" +
                    accountNumber + ": P" + balance);
            return balance;
        } catch (Exception e) {
            System.err.println("Balance check failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Account> getCustomerAccounts(int customerId) {
        try {
            List<Account> accounts = bankingService.getCustomerAccounts(customerId);
            System.out.println("Teller " + getFullName() + " retrieved " + accounts.size() +
                    " accounts for customer: " + customerId);
            return accounts;
        } catch (Exception e) {
            System.err.println("Failed to get customer accounts: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    public List<Transaction> getAccountTransactions(String accountNumber) {
        try {
            List<Transaction> transactions = bankingService.getAccountTransactions(accountNumber);
            System.out.println("Teller " + getFullName() + " retrieved " + transactions.size() +
                    " transactions for account #" + accountNumber);
            return transactions;
        } catch (Exception e) {
            System.err.println("Failed to get transaction history: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Find customer
    public Customer findCustomer(int customerId) {
        try {
            Customer customer = bankingService.findCustomer(customerId);
            if (customer != null) {
                System.out.println("Teller " + getFullName() + " found customer: " +
                        customer.getDisplayName() + " (" + customerId + ")");
            } else {
                System.out.println("Teller " + getFullName() + " - customer not found: " + customerId);
            }
            return customer;
        } catch (Exception e) {
            System.err.println("Customer search failed: " + e.getMessage());
            return null;
        }
    }

    private boolean validateAccountRequirements(Customer customer, String accountType) {
        accountType = accountType.toLowerCase().trim();

        if (accountType.equals("cheque")) {
            if (!(customer instanceof Individual)) {
                System.out.println("Cheque accounts only available for individual customers");
                return false;
            }
            Individual ind = (Individual) customer;
            if (!ind.hasVerifiedIncome()) {
                System.out.println("Cheque account requires verified employment income");
                return false;
            }
        } else if (accountType.equals("investment")) {
            if (customer instanceof Individual ind) {
                if (ind.getMonthlyIncome() < 500.0) {
                    System.out.println("Individuals need minimum P500 monthly income for investment account");
                    return false;
                }
            }
        }
        return true;
    }
}
