package com.example.bankaccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JDBCTransactionDAO implements TransactionDAO {
    private Connection connection;

    public JDBCTransactionDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount, balance_after, description) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, transaction.getAccountNumber());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setDouble(4, transaction.getBalance());
            stmt.setString(5, transaction.getDescription());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                transaction.setTransactionId(rs.getInt(1));
            }
        }
    }

    @Override
    public List<Transaction> findTransactionsByAccount(int accountNumber) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_date DESC";
        return fetchTransactions(accountNumber, sql);
    }

    @Override
    public List<Transaction> getRecentCustomerTransactions(int accountNumber, String customerId, int limit) throws SQLException {
        String sql = "SELECT t.* FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.account_number = ? AND a.customer_id = ? " +
                "ORDER BY t.transaction_date DESC LIMIT ?";
        return fetchTransactions(accountNumber, customerId, limit, sql);
    }

    @Override
    public List<Transaction> getCustomerTransactionsByDateRange(int accountNumber, String customerId,
                                                                Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT t.* FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.account_number = ? AND a.customer_id = ? " +
                "AND t.transaction_date BETWEEN ? AND ? " +
                "ORDER BY t.transaction_date DESC";

        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountNumber);
            stmt.setString(2, customerId);
            stmt.setDate(3, new java.sql.Date(startDate.getTime()));
            stmt.setDate(4, new java.sql.Date(endDate.getTime()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    private List<Transaction> fetchTransactions(int accountNumber, String sql) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    private List<Transaction> fetchTransactions(int accountNumber, String customerId, int limit, String sql) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountNumber);
            stmt.setString(2, customerId);
            stmt.setInt(3, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction(
                rs.getInt("account_number"),
                rs.getString("transaction_type"),
                rs.getDouble("amount"),
                rs.getDouble("balance_after"),
                rs.getString("description")
        );

        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setTransactionDate(new Date(rs.getTimestamp("transaction_date").getTime()));

        return transaction;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Error closing JDBCTransactionDAO connection", e);
            }
        }
    }
}
