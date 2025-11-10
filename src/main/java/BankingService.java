package com.example.bankaccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BankingService {
    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;
    private JDBCTransactionDAO transactionDAO;

    public BankingService() {
        try {
            this.customerDAO = new JDBCCustomerDAO();
            this.accountDAO = new JDBCAccountDAO();

            // connection passed to JDBC Transaction
            Connection connection = DatabaseConnection.getConnection();
            this.transactionDAO = new JDBCTransactionDAO(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize BankingService", e);
        }
    }

    public void createCustomer(Customer customer) {
        if (customerDAO.customerExists(customer.getIdentificationNumber())) {
            throw new IllegalArgumentException("Customer already exists with ID: " + customer.getIdentificationNumber());
        }
        customerDAO.saveCustomer(customer);
    }

    public Customer findCustomer(String id) {
        return customerDAO.findCustomerById(id);
    }

    public boolean customerExists(String id) {
        return customerDAO.customerExists(id);
    }

    public Account createAccount(BankTeller teller, Customer customer, String accountType, String branch, double initialDeposit) {
        if (!validateMinimumDeposit(accountType, initialDeposit)) {
            throw new IllegalArgumentException("Initial deposit does not meet minimum requirements for " + accountType + " account");
        }

        Account newAccount = null;
        switch (accountType.toLowerCase()) {
            case "savings":
                newAccount = new SavingsAccount(branch, customer);
                break;

            case "cheque":
                if (!(customer instanceof Individual)) {
                    throw new IllegalArgumentException("Cheque accounts are only available for individual customers");
                }

                Individual individual = (Individual) customer;
                if (!individual.hasVerifiedIncome()) {
                    throw new IllegalArgumentException("Cheque account requires verified employment income");
                }

                new ChequeAccount(branch, individual, initialDeposit);

                break;

            case "investment":
                newAccount = new InvestmentAccount(branch, customer, initialDeposit);
                break;
            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }

        accountDAO.saveAccount(newAccount);

        if (initialDeposit > 0) {
            newAccount.deposit(initialDeposit);
            accountDAO.updateAccount(newAccount); // Update with new balance

            Transaction depositTransaction = new Transaction(
                    newAccount.getAccountNumber(),
                    "INITIAL_DEPOSIT",
                    initialDeposit,
                    newAccount.getBalance(),
                    "Account opening deposit - Opened by: " + teller.getFullName()
            );
            try {
                transactionDAO.saveTransaction(depositTransaction);
            } catch (SQLException e) {
                handleDatabaseError("saveTransaction in createAccount", e);
            }
        }

        return newAccount;
    }

    public void deposit(int accountNumber, double amount, String description) {
        Account account = accountDAO.findAccountByNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        account.deposit(amount);
        accountDAO.updateAccount(account);

        Transaction transaction = new Transaction(
                accountNumber, "DEPOSIT", amount, account.getBalance(),
                description != null ? description : "Cash deposit"
        );
        try {
            transactionDAO.saveTransaction(transaction);
        } catch (SQLException e) {
            handleDatabaseError("saveTransaction in deposit", e);
        }
    }

    public void withdraw(int accountNumber, double amount, String description) {
        Account account = accountDAO.findAccountByNumber(accountNumber);

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        // Only allow withdrawal if account implements Withdrawable
        if (account instanceof Withdrawable withdrawableAccount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be greater than 0");
            }

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
            throw new UnsupportedOperationException("Withdrawals are not allowed for this account type");
        }
    }


    public List<Account> getCustomerAccounts(String customerId) {
        return accountDAO.findAccountsByCustomer(customerId);
    }

    public Account getAccount(int accountNumber) {
        return accountDAO.findAccountByNumber(accountNumber);
    }

    public List<Transaction> getAccountTransactions(int accountNumber) {
        try {
            return transactionDAO.findTransactionsByAccount(accountNumber);
        } catch (SQLException e) {
            handleDatabaseError("findTransactionsByAccount", e);
            return List.of(); // Return empty list on error
        }
    }

    public double getAccountBalance(int accountNumber) {
        Account account = accountDAO.findAccountByNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account.getBalance();
    }

    // Validation Methods
    private boolean validateMinimumDeposit(String accountType, double deposit) {
        double minimumDeposit = 0;

        switch (accountType.toLowerCase()) {
            case "savings":
                minimumDeposit = 50.00;
                break;
            case "cheque":
                minimumDeposit = 100.00;
                break;
            case "investment":
                minimumDeposit = 500.00;
                break;
        }

        return deposit >= minimumDeposit;
    }

    public void applyMonthlyInterest() {
        List<Account> allAccounts = accountDAO.findAllAccounts();
        for (Account account : allAccounts) {
            if (account instanceof InterestBearing) {
                InterestBearing interestAccount = (InterestBearing) account;
                interestAccount.applyInterest();
                accountDAO.updateAccount(account);
            }
        }
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAllCustomers();
    }

    public void close() {
        if (transactionDAO != null) {
            transactionDAO.close();
        }
    }

    private void handleDatabaseError(String operation, SQLException e) {
        String timestamp = new java.util.Date().toString();
        System.err.printf("⏰ [%s] ❌ Database Error in BankingService.%s%n", timestamp, operation);
        System.err.println("   🔍 SQL State: " + e.getSQLState());
        System.err.println("   🔍 Error Code: " + e.getErrorCode());
        System.err.println("   📝 Message: " + e.getMessage());
        throw new RuntimeException("Database operation failed: " + operation, e);
    }
}