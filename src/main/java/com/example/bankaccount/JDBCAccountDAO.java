package com.example.bankaccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import static javax.management.remote.JMXConnectorFactory.connect;

public class JDBCAccountDAO implements AccountDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/bankingdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private final CustomerDAO customerDAO;

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public JDBCAccountDAO(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    private String generateAccountNumber() {
        // Always starts with 7, then 11 random digits
        StringBuilder sb = new StringBuilder("7");
        for (int i = 0; i < 11; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    private Account createAccountObject(String type, String branch, Customer customer) {
        if (type == null) return null;

        String normalized = type.trim().toLowerCase();

        switch (normalized) {
            case "savings":
                return new SavingsAccount(branch, customer);
            case "investment":
                return new InvestmentAccount(branch, customer, 0.0);
            case "cheque":
                return new ChequeAccount(branch, customer);
            default:
                System.out.println("Unknown account type: " + type);
                return null;
        }
    }


    @Override
    public void saveAccount(Account account) {
        // 1️⃣ Generate account number
        String accNum = generateAccountNumber();
        account.setAccountNumber(accNum);

// 2️⃣ Ensure lastInterestDate is set for all accounts
        if (account instanceof InterestBearing interestAcc) {
            // For interest-bearing accounts, use existing lastInterestDate or customer registration date
            if (interestAcc.getLastInterestDate() == null) {
                interestAcc.setLastInterestDate(account.getCustomer().getRegistrationDate());
            }
        } else {
            // For non-interest-bearing accounts, just set lastInterestDate to customer's registration date
            account.setLastInterestDate(account.getCustomer().getRegistrationDate());
        }

        // 3️⃣ Determine account type string
        String accountType;
        if (account instanceof SavingsAccount) accountType = "savings";
        else if (account instanceof InvestmentAccount) accountType = "investment";
        else if (account instanceof ChequeAccount) accountType = "cheque";
        else accountType = "unknown";

        // 4️⃣ Prepare SQL
        String sql;
        boolean isInterest = account instanceof InterestBearing;
        if (isInterest) {
            sql = "INSERT INTO account (accountNumber, balance, branch, accountType, lastInterestDate, customerId) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO account (accountNumber, balance, branch, accountType, customerId) VALUES (?, ?, ?, ?, ?)";
        }

        // 5️⃣ Execute insert
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, account.getAccountNumber());
            stmt.setDouble(paramIndex++, account.getBalance());
            stmt.setString(paramIndex++, account.getBranch());
            stmt.setString(paramIndex++, accountType);

            if (isInterest) {
                java.util.Date lastDate = ((InterestBearing) account).getLastInterestDate();

                stmt.setDate(paramIndex++, new java.sql.Date(lastDate.getTime()));
            }

            stmt.setInt(paramIndex, account.getCustomer().getCustomerId());

            stmt.executeUpdate();

            System.out.println("Account " + accNum + " saved successfully!");

        } catch (SQLException e) {
            throw new RuntimeException("Error saving new account", e);
        }
    }


    @Override
    public Account findAccountByNumber(String accountNumber) {
        String sql = "SELECT * FROM account WHERE accountNumber = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type = rs.getString("accountType");
                int customerId = rs.getInt("customerId");

                Customer customer = new JDBCCustomerDAO().findCustomerById(customerId);

                Account acc = createAccountObject(type, branch, customer);
                acc.setAccountNumber(accountNumber);
                acc.setBalance(balance);

                return acc;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by number", e);
        }

        return null;
    }

    @Override
    public List<Account> findAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM account";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Create a single TransactionDAO to pass to interest-bearing accounts
            JDBCTransactionDAO transactionDAO = new JDBCTransactionDAO(conn);

            while (rs.next()) {
                String accountNumber = rs.getString("accountNumber");
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                
                String type = rs.getString("accountType")
                        .trim()
                        .toLowerCase()
                        .replace("account", "");
                int customerId = rs.getInt("customerId");

                Customer customer = customerDAO.findCustomerById(customerId);
                if (customer == null) {
                    System.out.println("Skipping account " + accountNumber + ": customer not found.");
                    continue;
                }

                Account acc = null;

                switch (type) {
                    case "savings":
                        acc = new SavingsAccount(branch, customer, transactionDAO);
                        break;
                    case "investment":
                        InvestmentAccount invAcc = new InvestmentAccount(branch, customer, transactionDAO);
                        invAcc.setBalance(balance);
                        acc = invAcc;
                        break;
                    case "cheque":
                        if (customer instanceof Individual) {
                            acc = new ChequeAccount(branch, customer);
                        } else {
                            System.out.println("Skipping Cheque account " + accountNumber + " for non-individual customer");
                        }
                        break;
                    default:
                        System.out.println("Skipping unknown account type " + type + " for account " + accountNumber);
                }

                if (acc != null) {
                    // set lastInterestDate for interest-bearing accounts
                    if (acc instanceof InterestBearing interestAcc) {
                        java.sql.Date sqlDate = rs.getDate("lastInterestDate");
                        if (sqlDate != null) {
                            interestAcc.setLastInterestDate(new java.util.Date(sqlDate.getTime()));
                        }
                    }

                    acc.setAccountNumber(accountNumber);
                    acc.setBalance(balance);
                    accounts.add(acc);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching accounts", e);
        }

        return accounts;
    }

    @Override
    public List<Account> findAccountsByCustomer(int customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM account WHERE customerId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String accountNumber = rs.getString("accountNumber");
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type = rs.getString("accountType").trim().toLowerCase().replace("account", "");
                Date lastInterest = rs.getDate("lastInterestDate");

                Customer customer = new JDBCCustomerDAO().findCustomerById(customerId);
                if (customer == null) {
                    System.out.println("Customer " + customerId + " not found. Skipping account " + accountNumber);
                    continue;
                }

                Account acc = createAccountObject(type, branch, customer);
                if (acc == null) {
                    System.out.println("Unknown account type: " + type + " for account " + accountNumber);
                    continue;
                }

                acc.setAccountNumber(accountNumber);
                acc.setBalance(balance);

                if (acc instanceof InterestBearing interestAcc) {
                    if (lastInterest != null) {
                        interestAcc.setLastInterestDate(lastInterest);
                    } else {
                        interestAcc.setLastInterestDate(customer.getRegistrationDate());
                    }
                }

                accounts.add(acc);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding accounts for customer", e);
        }

        return accounts;
    }

    @Override
    public void updateAccount(Account account) {
        String sql;

        if (account instanceof InterestBearing) {
            sql = "UPDATE account SET balance = ?, lastInterestDate = ?, branch = ? WHERE accountNumber = ?";
        } else {
            sql = "UPDATE account SET balance = ?, branch = ? WHERE accountNumber = ?";
        }

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, account.getBalance());

            if (account instanceof InterestBearing interestAcc) {
                stmt.setDate(2, new java.sql.Date(interestAcc.getLastInterestDate().getTime()));
                stmt.setString(3, account.getBranch());
                stmt.setString(4, account.getAccountNumber());
            } else {
                stmt.setString(2, account.getBranch());
                stmt.setString(3, account.getAccountNumber());
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating account", e);
        }
    }

    @Override
    public void deleteAccount(String accountNumber) {
        String sql = "DELETE FROM account WHERE accountNumber = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account", e);
        }
    }

    @Override
    public boolean accountExists(String accountNumber) {
        String sql = "SELECT 1 FROM account WHERE accountNumber = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Error checking account existence", e);
        }
    }

    @Override
    public List<Account> getRecentlyCreatedAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM account ORDER BY createdAt DESC LIMIT 10";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String number = rs.getString("accountNumber");
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type = rs.getString("accountType");
                int customerId = rs.getInt("customerId");
                Date lastInterest = rs.getDate("lastInterestDate");

                Customer customer = new JDBCCustomerDAO().findCustomerById(customerId);

                Account acc = createAccountObject(type, branch, customer);
                acc.setAccountNumber(number);
                acc.setBalance(balance);

                if (acc instanceof InterestBearing interestAcc) {
                    interestAcc.setLastInterestDate(lastInterest);
                }

                accounts.add(acc);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading recent accounts", e);
        }

        return accounts;
    }

    @Override
    public List<Account> getAccountsForCustomer(int customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM account WHERE customerId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Map ResultSet to Account object
                String accountNumber = rs.getString("accountNumber");
                String type = rs.getString("accountType");
                double balance = rs.getDouble("balance");
                String branch = rs.getString("branch");

                Account acc = null;
                switch (type.toLowerCase()) {
                    case "savings":
                        acc = new SavingsAccount(branch, null, balance);
                        break;
                    case "cheque":
                        acc = new ChequeAccount(branch, null, balance);
                        break;
                    case "investment":
                        acc = new InvestmentAccount(branch, null, balance);
                        break;
                }

                if (acc != null) {
                    acc.setAccountNumber(accountNumber);
                    accounts.add(acc);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }

}
