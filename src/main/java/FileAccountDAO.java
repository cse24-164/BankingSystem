package com.example.bankaccount;

import java.io.*;
import java.util.*;

public class FileAccountDAO implements AccountDAO {
    private static final String ACCOUNT_FILE = "accounts.txt";
    private List<Account> accountsList = new ArrayList<>();
    private CustomerDAO customerDAO;

    public FileAccountDAO() {
        this.customerDAO = new FileCustomerDAO();
        loadAccountsFromFile();
    }

    @Override
    public void saveAccount(Account account) {
        Account existing = findAccountByNumber(account.getAccountNumber());
        if (existing != null) {
            accountsList.remove(existing);
        }
        accountsList.add(account);
        saveAccountsToFile();
        System.out.println("✅ Account saved: #" + account.getAccountNumber() +
                " (" + account.getCustomer().getDisplayName() + ")");
    }

    @Override
    public Account findAccountByNumber(int accountNumber) {
        for (Account account : accountsList) {
            if (account.getAccountNumber() == accountNumber) {
                return account;
            }
        }
        return null;
    }

    @Override
    public List<Account> findAllAccounts() {
        return new ArrayList<>(accountsList);
    }

    @Override
    public List<Account> findAccountsByCustomer(String customerId) {
        List<Account> customerAccounts = new ArrayList<>();
        for (Account account : accountsList) {
            if (account.getCustomer().getIdentificationNumber().equals(customerId)) {
                customerAccounts.add(account);
            }
        }
        System.out.println("📋 Found " + customerAccounts.size() + " accounts for customer: " + customerId);
        return customerAccounts;
    }

    @Override
    public void updateAccount(Account account) {
        Account existing = findAccountByNumber(account.getAccountNumber());
        if (existing != null) {
            accountsList.remove(existing);
            accountsList.add(account);
            saveAccountsToFile();
            System.out.println("✅ Account updated: #" + account.getAccountNumber());
        }
    }

    @Override
    public void deleteAccount(int accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account != null) {
            accountsList.remove(account);
            saveAccountsToFile();
            System.out.println("✅ Account deleted: #" + accountNumber);
        }
    }

    @Override
    public boolean accountExists(int accountNumber) {
        return findAccountByNumber(accountNumber) != null;
    }

    private void loadAccountsFromFile() {
        File file = new File(ACCOUNT_FILE);
        if (!file.exists()) {
            System.out.println("📁 No existing accounts file found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String line;
            int loadedCount = 0;

            while ((line = reader.readLine()) != null) {
                Account account = parseAccount(line);
                if (account != null) {
                    accountsList.add(account);
                    loadedCount++;
                }
            }

            System.out.println("✅ Loaded " + loadedCount + " accounts from file");

        } catch (IOException e) {
            System.err.println("❌ Error reading accounts file: " + e.getMessage());
        }
    }

    private void saveAccountsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ACCOUNT_FILE))) {
            for (Account account : accountsList) {
                writer.println(formatAccount(account));
            }
            System.out.println("💾 Saved " + accountsList.size() + " accounts to file");
        } catch (IOException e) {
            System.err.println("❌ Error saving accounts to file: " + e.getMessage());
        }
    }

    private Account parseAccount(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 5) {
                System.err.println("❌ Invalid account line: " + line);
                return null;
            }

            String accountType = parts[0];
            int accountNumber = Integer.parseInt(parts[1]);
            String customerId = parts[2];
            String branch = parts[3];
            double balance = Double.parseDouble(parts[4]);

            // Find the customer
            Customer customer = customerDAO.findCustomerById(customerId);
            if (customer == null) {
                System.err.println("❌ Customer not found: " + customerId);
                return null;
            }

            // Create account based on type
            Account account = createAccount(accountType, branch, customer, parts);
            if (account == null) {
                return null;
            }

            // SET THE ACCOUNT NUMBER USING YOUR NEW METHOD
            account.setAccountNumber(accountNumber);

            // Set the balance
            setAccountBalance(account, balance);

            System.out.println("✅ Loaded account: #" + accountNumber + " (" + accountType +
                    ") for " + customer.getDisplayName() + " | Balance: P" + balance);
            return account;

        } catch (Exception e) {
            System.err.println("❌ Error parsing account: " + line);
            e.printStackTrace();
            return null;
        }
    }

    private Account createAccount(String accountType, String branch, Customer customer, String[] parts) {
        switch (accountType) {
            case "SAVINGS":
                return new SavingsAccount(branch, customer);

            case "CHEQUE":
                if (customer instanceof Individual) {
                    Individual individual = (Individual) customer;
                    IncomeSource employment = individual.getIncomeSource();
                    if (employment == null) {
                        employment = new IncomeSource(
                                "Default Employer", "EMPLOYMENT", 5000.0,
                                "Default employment income", "123 Employer St"
                        );
                    }
                    return new ChequeAccount(branch, customer, employment);
                }
                System.err.println("❌ Company cannot have cheque account");
                return null;

            case "INVESTMENT":
                // For investment accounts, use the balance as initial deposit if not specified
                double initialDeposit = parts.length > 5 ? Double.parseDouble(parts[5]) : Double.parseDouble(parts[4]);
                return new InvestmentAccount(branch, customer, initialDeposit);

            default:
                System.err.println("❌ Unknown account type: " + accountType);
                return null;
        }
    }

    private void setAccountBalance(Account account, double balance) {
        try {
            // Simple approach - use deposit to set initial balance
            if (balance > 0) {
                // Use reflection to reset to zero first to avoid double counting
                java.lang.reflect.Field balanceField = Account.class.getDeclaredField("balance");
                balanceField.setAccessible(true);
                balanceField.set(account, 0.0);

                // Now deposit the correct amount
                account.deposit(balance);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not set balance properly");
            // Fallback - this might create a transaction but it will work
            if (balance > 0) {
                account.deposit(balance);
            }
        }
    }

    private String formatAccount(Account account) {
        StringBuilder sb = new StringBuilder();

        if (account instanceof SavingsAccount) {
            sb.append("SAVINGS");
        } else if (account instanceof ChequeAccount) {
            sb.append("CHEQUE");
        } else if (account instanceof InvestmentAccount) {
            sb.append("INVESTMENT");
            // For investment accounts, we'll store the current balance as both balance and initial deposit
            // since we don't have access to getInitialDeposit()
            InvestmentAccount investment = (InvestmentAccount) account;
            sb.append("|").append(account.getAccountNumber())
                    .append("|").append(account.getCustomer().getIdentificationNumber())
                    .append("|").append(account.getBranch())
                    .append("|").append(String.format("%.2f", account.getBalance()))
                    .append("|").append(String.format("%.2f", account.getBalance())); // Use balance as initial deposit
            return sb.toString();
        } else {
            sb.append("UNKNOWN");
        }

        sb.append("|").append(account.getAccountNumber())
                .append("|").append(account.getCustomer().getIdentificationNumber())
                .append("|").append(account.getBranch())
                .append("|").append(String.format("%.2f", account.getBalance()));

        return sb.toString();
    }

    @Override
    public List<Account> getRecentlyCreatedAccounts(int limit) {
        int startIndex = Math.max(0, accountsList.size() - limit);
        List<Account> recentAccounts = new ArrayList<>();

        for (int i = accountsList.size() - 1; i >= startIndex && i >= 0; i--) {
            recentAccounts.add(accountsList.get(i));
        }
        return recentAccounts;
    }

    // Debug method
    public void debugPrintAllAccounts() {
        System.out.println("=== ALL ACCOUNTS DEBUG ===");
        for (Account acc : accountsList) {
            System.out.println("Account #" + acc.getAccountNumber() +
                    " | Customer: " + acc.getCustomer().getIdentificationNumber() +
                    " | Type: " + acc.getClass().getSimpleName() +
                    " | Balance: P" + acc.getBalance());
        }
        System.out.println("Total: " + accountsList.size() + " accounts");
        System.out.println("=========================");
    }
}