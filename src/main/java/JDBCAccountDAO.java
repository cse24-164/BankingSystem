package com.example.bankaccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCAccountDAO implements AccountDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/bankingdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @Override
    public void saveAccount(Account account) {
        String sql = "INSERT INTO accounts (accountNumber, balance, branch, customerId, accountType) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, account.getAccountNumber());
            stmt.setDouble(2, account.getBalance());
            stmt.setString(3, account.getBranch());

            Customer customer = account.getCustomer();
            String customerIdValue;
            if (customer instanceof Individual) {
                customerIdValue = ((Individual) customer).getIdNumber();
            } else if (customer instanceof Company) {
                customerIdValue = ((Company) customer).getRegistrationNumber();
            } else {
                throw new IllegalArgumentException("Unknown customer type");
            }
            stmt.setString(4, customerIdValue);

            stmt.setString(5, account.getClass().getSimpleName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving account", e);
        }
    }


    @Override
    public Account findAccountByNumber(int accountNumber) {
        String sql = "SELECT * FROM accounts WHERE accountNumber = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type = rs.getString("accountType");

                Account acc = createAccountObject(type, branch, null);
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
        String sql = "SELECT * FROM accounts";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int number = rs.getInt("accountNumber");
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type = rs.getString("accountType");

                Account acc = createAccountObject(type, branch, null);
                acc.setAccountNumber(number);
                acc.setBalance(balance);

                accounts.add(acc);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all accounts", e);
        }

        return accounts;
    }

    @Override
    public List<Account> findAccountsByCustomer(String customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customerId = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int number = rs.getInt("accountNumber");
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type = rs.getString("accountType");

                Account acc = createAccountObject(type, branch, null);
                acc.setAccountNumber(number);
                acc.setBalance(balance);
                accounts.add(acc);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding accounts by customer", e);
        }

        return accounts;
    }

    @Override
    public void updateAccount(Account account) {
        String sql = "UPDATE accounts SET balance = ?, branch = ? WHERE accountNumber = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, account.getBalance());
            stmt.setString(2, account.getBranch());
            stmt.setInt(3, account.getAccountNumber());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating account", e);
        }
    }

    @Override
    public void deleteAccount(int accountNumber) {
        String sql = "DELETE FROM accounts WHERE accountNumber = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountNumber);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account", e);
        }
    }

    @Override
    public boolean accountExists(int accountNumber) {
        String sql = "SELECT 1 FROM accounts WHERE accountNumber = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Error checking if account exists", e);
        }
    }

    @Override
    public List<Account> getRecentlyCreatedAccounts(int limit) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY accountNumber DESC LIMIT ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int number = rs.getInt("accountNumber");
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type = rs.getString("accountType");

                Account acc = createAccountObject(type, branch, null);
                acc.setAccountNumber(number);
                acc.setBalance(balance);
                accounts.add(acc);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving recent accounts", e);
        }

        return accounts;
    }

    private Account createAccountObject(String type, String branch, Customer customer) {
        return switch (type) {
            case "ChequeAccount" -> new ChequeAccount(branch, customer);
            case "SavingsAccount" -> new SavingsAccount(branch, customer);
            case "InvestmentAccount" -> new InvestmentAccount(branch, customer, 500.0);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }
}
