package com.example.bankaccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class BankingService {

    private AccountDAO accountDAO;
    private JDBCTransactionDAO transactionDAO;
    private CustomerDAO customerDAO;

    public BankingService() {
        try {
            this.customerDAO = new JDBCCustomerDAO();
            this.accountDAO = new JDBCAccountDAO(customerDAO);

            Connection connection = DatabaseConnection.getConnection();
            this.transactionDAO = new JDBCTransactionDAO(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize BankingService", e);
        }
    }

    // ---------------- Customer Management ----------------
    public void createCustomer(Customer customer) {
        if (customerDAO.customerExists(customer.getCustomerId())) {
            throw new IllegalArgumentException("Customer already exists with ID: " + customer.getCustomerId());
        }
        customerDAO.saveCustomer(customer);
    }

    public Customer findCustomer(int customerId) {
        return customerDAO.findCustomerById(customerId);
    }

    public Customer findCustomerById(int customerId) {
        return customerDAO.findCustomerById(customerId);
    }

    public boolean customerExists(int customerId) {
        return customerDAO.customerExists(customerId);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAllCustomers();
    }

    // ---------------- Account Management ----------------
    public Account createAccount(BankTeller teller, Customer customer,
                                 String accountType, String branch, double initialDeposit) {

        // Validate minimum deposit
        if (!validateMinimumDeposit(accountType, initialDeposit)) {
            throw new IllegalArgumentException("Initial deposit does not meet minimum requirements for " + accountType);
        }

        Account newAccount;

        switch (accountType.toLowerCase()) {
            case "savings":
                newAccount = new SavingsAccount(branch, customer, initialDeposit);
                break;

            case "cheque":
                if (!(customer instanceof Individual)) {
                    throw new IllegalArgumentException("Cheque accounts are only available for individual customers");
                }
                Individual ind = (Individual) customer;
                if (!ind.hasVerifiedIncome()) {
                    throw new IllegalArgumentException("Cheque account requires verified employment income");
                }
                newAccount = new ChequeAccount(branch, ind, initialDeposit);
                break;

            case "investment":
                newAccount = new InvestmentAccount(branch, customer, initialDeposit);
                break;

            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }

        // Save account to database
        accountDAO.saveAccount(newAccount);

        // Record initial deposit transaction if > 0
        if (initialDeposit > 0) {
            Transaction depositTransaction = new Transaction(
                    newAccount.getAccountNumber(),
                    "INITIAL_DEPOSIT",
                    initialDeposit,
                    newAccount.getBalance(),
                    "Account opening deposit by: " + teller.getFullName()
            );
            try {
                transactionDAO.saveTransaction(depositTransaction);
            } catch (SQLException e) {
                handleDatabaseError("saveTransaction in createAccount", e);
            }
        }
        // Add account to customer's in-memory list
        customer.addAccount(newAccount);

        return newAccount;
    }

    public Account createAccountForExistingCustomer(
            BankTeller teller,
            Customer customer,
            String accountType,
            String branch,
            double initialDeposit
    ) {

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null when creating an account.");
        }

        if (teller == null) {
            throw new IllegalArgumentException("Teller cannot be null – account creation must be authorized.");
        }

        if (accountType == null || accountType.isBlank()) {
            throw new IllegalArgumentException("Account type is required.");
        }

        // Validate minimum deposit
        if (!validateMinimumDeposit(accountType, initialDeposit)) {
            throw new IllegalArgumentException(
                    "Initial deposit does not meet minimum requirements for " + accountType
            );
        }

        Account newAccount;

        // ---- CREATE ACCOUNT BASED ON TYPE ----
        switch (accountType.toLowerCase().trim()) {

            case "savings" -> newAccount = new SavingsAccount(branch, customer, initialDeposit);

            case "cheque" -> {
                if (!(customer instanceof Individual)) {
                    throw new IllegalArgumentException("Cheque accounts are only available for Individual customers.");
                }
                Individual ind = (Individual) customer;
                if (!ind.hasVerifiedIncome()) {
                    throw new IllegalArgumentException("Cheque account requires verified employment income.");
                }
                newAccount = new ChequeAccount(branch, ind, initialDeposit);
            }

            case "investment" -> newAccount = new InvestmentAccount(branch, customer, initialDeposit);

            default -> throw new IllegalArgumentException("Unknown account type: " + accountType);
        }

        try {
            // ---- SAVE ACCOUNT TO DATABASE ----
            accountDAO.saveAccount(newAccount);

            // ---- RECORD INITIAL DEPOSIT TRANSACTION ----
            if (initialDeposit > 0) {
                Transaction depositTransaction = new Transaction(
                        newAccount.getAccountNumber(),
                        "INITIAL_DEPOSIT",
                        initialDeposit,
                        newAccount.getBalance(),
                        "Account opened by teller: " + teller.getFullName()
                );
                transactionDAO.saveTransaction(depositTransaction);
            }

            if (customer.getAccounts() == null) {
                customer.setAccounts(new ArrayList<>());
            }
            customer.addAccount(newAccount);

            return newAccount;

        } catch (SQLException e) {
            handleDatabaseError("createAccountForExistingCustomer", e);
            throw new RuntimeException("Failed to create account for existing customer.");
        }
    }


    public void deposit(String accountNumber, double amount, String description) {
        Account account = accountDAO.findAccountByNumber(accountNumber);
        if (account == null) throw new IllegalArgumentException("Account not found: " + accountNumber);

        account.deposit(amount);
        accountDAO.updateAccount(account);

        Transaction transaction = new Transaction(
                accountNumber,
                "DEPOSIT",
                amount,
                account.getBalance(),
                description != null ? description : "Cash deposit"
        );

        try {
            transactionDAO.saveTransaction(transaction);
        } catch (SQLException e) {
            handleDatabaseError("saveTransaction in deposit", e);
        }
    }

    public void withdraw(String accountNumber, double amount, String description) {
        Account account = accountDAO.findAccountByNumber(accountNumber);
        if (account == null) throw new IllegalArgumentException("Account not found: " + accountNumber);

        if (account instanceof Withdrawable withdrawableAccount) {
            withdrawableAccount.withdraw(amount);
            accountDAO.updateAccount(account);

            Transaction transaction = new Transaction(
                    accountNumber,
                    "WITHDRAWAL",
                    amount,
                    account.getBalance(),
                    description != null ? description : "Cash withdrawal"
            );

            try {
                transactionDAO.saveTransaction(transaction);
            } catch (SQLException e) {
                handleDatabaseError("saveTransaction in withdraw", e);
            }
        } else {
            throw new UnsupportedOperationException("Withdrawals not allowed for this account type");
        }
    }

    // BankingService.java
    public void updateAccount(Account account) {
        accountDAO.updateAccount(account);
    }

    // ---------------- Queries ----------------
    public List<Account> getAllAccounts() {
        return accountDAO.findAllAccounts();
    }
    public List<Account> getCustomerAccounts(int customerId) {
        return accountDAO.findAccountsByCustomer(customerId);
    }

    public Account getAccount(String accountNumber) {
        return accountDAO.findAccountByNumber(accountNumber);
    }

    public List<Transaction> getAccountTransactions(String accountNumber) {
        try {
            return transactionDAO.findTransactionsByAccount(accountNumber);
        } catch (SQLException e) {
            handleDatabaseError("findTransactionsByAccount", e);
            return List.of();
        }
    }

    public double getAccountBalance(String accountNumber) {
        Account account = accountDAO.findAccountByNumber(accountNumber);
        if (account == null) throw new IllegalArgumentException("Account not found: " + accountNumber);
        return account.getBalance();
    }

    // ---------------- Utilities ----------------
    private boolean validateMinimumDeposit(String accountType, double deposit) {
        if (accountType.equalsIgnoreCase("cheque")) {
            return deposit >= 0;
        } else {
            return deposit > 0;
        }
    }

    public void close() {
        if (transactionDAO != null) transactionDAO.close();
    }

    private void handleDatabaseError(String operation, SQLException e) {
        String timestamp = new java.util.Date().toString();
        System.err.printf("[%s] Database Error in BankingService.%s%n", timestamp, operation);
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("Message: " + e.getMessage());
        throw new RuntimeException("Database operation failed: " + operation, e);
    }

}
