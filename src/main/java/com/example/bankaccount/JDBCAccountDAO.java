package com.example.bankaccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static javax.management.remote.JMXConnectorFactory.connect;

public class JDBCAccountDAO implements AccountDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/bankingdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private String generateAccountNumber() {
        // Always starts with 7, then 11 random digits
        StringBuilder sb = new StringBuilder("7");
        for (int i = 0; i < 11; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    @Override
    public void saveAccount(Account account) {
        String accNum = generateAccountNumber();
        account.setAccountNumber(accNum);

        String sql;
        if (account instanceof InterestBearing) {
            sql = "UPDATE account SET balance = ?, branch = ?, lastInterestDate = ? WHERE accountNumber = ?";
        } else {
            sql = "UPDATE account SET balance = ?, branch = ? WHERE accountNumber = ?";
        }

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, account.getBalance());
            stmt.setString(2, account.getBranch());

            if (account instanceof InterestBearing interestAcc) {
                java.util.Date lastDate = interestAcc.getLastInterestDate();
                stmt.setDate(3, new java.sql.Date(lastDate.getTime()));
                stmt.setString(4, account.getAccountNumber());
            } else {
                stmt.setString(3, account.getAccountNumber());
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating account", e);
        }
    }

        @Override
        public Account findAccountByNumber (String accountNumber){
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
        public List<Account> findAllAccounts () {
            List<Account> accounts = new ArrayList<>();
            String sql = "SELECT * FROM account";

            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String number = rs.getString("accountNumber");
                    String branch = rs.getString("branch");
                    double balance = rs.getDouble("balance");
                    String type = rs.getString("accountType");
                    int customerId = rs.getInt("customerId");

                    Customer customer = new JDBCCustomerDAO().findCustomerById(customerId);

                    Account acc = createAccountObject(type, branch, customer);
                    acc.setAccountNumber(number);
                    acc.setBalance(balance);

                    accounts.add(acc);
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error retrieving accounts", e);
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
                String type = rs.getString("accountType");
                Date lastInterest = rs.getDate("lastInterestDate");

                Customer customer = new JDBCCustomerDAO().findCustomerById(customerId);

                Account acc = createAccountObject(type, branch, customer);
                acc.setAccountNumber(accountNumber);
                acc.setBalance(balance);

                // Set lastInterestDate only if the account is interest-bearing
                if (acc instanceof InterestBearing interestAcc) {
                    interestAcc.setLastInterestDate(lastInterest);
                }

                accounts.add(acc);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding accounts for customer", e);
        }

        return accounts;
    }


@Override
        public void updateAccount (Account account){
            String sql;
            if (account instanceof InterestBearing) {
                sql = "UPDATE account SET balance = ?, branch = ?, lastInterestDate = ? WHERE accountNumber = ?";
            } else {
                sql = "UPDATE account SET balance = ?, branch = ? WHERE accountNumber = ?";
            }

            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDouble(1, account.getBalance());
                stmt.setString(2, account.getBranch());

                if (account instanceof InterestBearing interestAcc) {
                    java.util.Date lastDate = interestAcc.getLastInterestDate();
                    stmt.setDate(3, new java.sql.Date(lastDate.getTime()));
                    stmt.setString(4, account.getAccountNumber());
                } else {
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


    private Account createAccountObject(String type, String branch, Customer customer) {
        return switch (type) {
            case "SavingsAccount" -> new SavingsAccount(branch, customer);
            case "ChequeAccount" -> new ChequeAccount(branch, customer);
            case "InvestmentAccount" -> new InvestmentAccount(branch, customer, 0);
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
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
