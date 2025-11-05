package com.example.bankaccount;

import java.sql.*;
import java.util.*;

public class JDBCAccountDAO implements AccountDAO {

    @Override
    public void saveAccount(Account account) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, branch, balance) VALUES (?, ?, ?, ?, ?)";
        executeUpdate(sql,
                account.getAccountNumber(),
                account.getCustomer().getIdentificationNumber(),
                getAccountType(account),
                account.getBranch(),
                account.getBalance()
        );
    }

    @Override
    public Account findAccountByNumber(int accountNumber) {
        String sql = "SELECT a.*, c.customer_type FROM accounts a JOIN customers c ON a.customer_id = c.customer_id WHERE a.account_number = ?";
        return querySingle(sql, this::mapAccount, accountNumber);
    }

    @Override
    public List<Account> findAccountsByCustomer(String customerId) {
        String sql = "SELECT a.*, c.customer_type FROM accounts a JOIN customers c ON a.customer_id = c.customer_id WHERE a.customer_id = ?";
        return queryList(sql, this::mapAccount, customerId);
    }

    @Override
    public void updateAccount(Account account) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        executeUpdate(sql, account.getBalance(), account.getAccountNumber());
    }

    @Override
    public List<Account> getRecentlyCreatedAccounts(int limit) {
        String sql = "SELECT a.*, c.customer_type FROM accounts a JOIN customers c ON a.customer_id = c.customer_id ORDER BY a.created_date DESC FETCH FIRST ? ROWS ONLY";
        return queryList(sql, this::mapAccount, limit);
    }

    @Override
    public List<Account> findAllAccounts() {
        String sql = "SELECT a.*, c.customer_type FROM accounts a JOIN customers c ON a.customer_id = c.customer_id";
        return queryList(sql, this::mapAccount);
    }

    @Override
    public void deleteAccount(int accountNumber) {
        executeUpdate("DELETE FROM accounts WHERE account_number = ?", accountNumber);
    }

    @Override
    public boolean accountExists(int accountNumber) {
        return findAccountByNumber(accountNumber) != null;
    }

    // Helper methods
    private String getAccountType(Account account) {
        if (account instanceof SavingsAccount) return "SAVINGS";
        if (account instanceof ChequeAccount) return "CHEQUE";
        if (account instanceof InvestmentAccount) return "INVESTMENT";
        return "UNKNOWN";
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        String customerId = rs.getString("customer_id");

        // Get customer from database
        Customer customer = new JDBCCustomerDAO().findCustomerById(customerId);
        if (customer == null) throw new RuntimeException("Customer not found: " + customerId);

        Account account = switch (type) {
            case "SAVINGS" -> new SavingsAccount(rs.getString("branch"), customer);
            case "CHEQUE" -> {
                IncomeSource income = (customer instanceof Individual) ?
                        ((Individual) customer).getIncomeSource() : null;
                yield new ChequeAccount(rs.getString("branch"), customer, income);
            }
            case "INVESTMENT" -> new InvestmentAccount(rs.getString("branch"), customer, rs.getDouble("balance"));
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };

        account.setAccountNumber(rs.getInt("account_number"));
        setBalance(account, rs.getDouble("balance"));
        return account;
    }

    private void setBalance(Account account, double balance) {
        try {
            var field = Account.class.getDeclaredField("balance");
            field.setAccessible(true);
            field.set(account, balance);
        } catch (Exception e) {
            throw new RuntimeException("Can't set balance", e);
        }
    }

    // Database helpers (same as in JDBCCustomerDAO - consider extracting to a helper class)
    private void executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + sql, e);
        }
    }

    private <T> T querySingle(String sql, ResultMapper<T> mapper, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapper.map(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + sql, e);
        }
    }

    private <T> List<T> queryList(String sql, ResultMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + sql, e);
        }
    }

    @FunctionalInterface
    private interface ResultMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}