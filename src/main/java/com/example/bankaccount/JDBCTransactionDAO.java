package com.example.bankaccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JDBCTransactionDAO implements com.example.bankaccount.TransactionDAO {
    private Connection connection;

    public JDBCTransactionDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transaction (accountNumber, transactionType, amount, balance, description) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, transaction.getAccountNumber());
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
    public List<Transaction> findTransactionsByAccount(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM transaction WHERE accountNumber = ? ORDER BY transactionDate DESC";
        return fetchTransactions(accountNumber, sql);
    }

    @Override
    public List<Transaction> getRecentCustomerTransactions(String accountNumber, String accountType) {
        List<Transaction> transactions = new ArrayList<>();

        String sql = """
        SELECT *
        FROM transactions
        WHERE accountNumber = ? AND accountType = ?
        ORDER BY transactionDate DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            stmt.setString(2, accountType);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recent transactions for account", e);
        }

        return transactions;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        String accountNumber = rs.getString("accountNumber");

        Transaction transaction = new Transaction(
                accountNumber,                       // accountNumber as String
                rs.getString("transactionType"),
                rs.getDouble("amount"),
                rs.getDouble("balance"),
                rs.getString("description")
        );

        transaction.setTransactionId(rs.getInt("transactionId"));
        transaction.setTransactionDate(rs.getDate("transactionDate"));

        return transaction;
    }




    private List<Transaction> fetchTransactions(String accountNumber, String sql) throws SQLException {
        List<Transaction> transaction = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transaction.add(mapTransaction(rs));
            }
        }
        return transaction;
    }

    private List<Transaction> fetchTransactions(String accountNumber, String customerId, String sql) throws SQLException {
        List<Transaction> transaction = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, customerId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transaction.add(mapTransaction(rs));
            }
        }
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
