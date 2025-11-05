package com.example.bankaccount;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface TransactionDAO {
    void saveTransaction(Transaction transaction) throws SQLException;
    List<Transaction> findTransactionsByAccount(int accountNumber) throws SQLException;
    List<Transaction> getRecentCustomerTransactions(int accountNumber, String customerId, int limit) throws SQLException;
    List<Transaction> getCustomerTransactionsByDateRange(int accountNumber, String customerId,
                                                         Date startDate, Date endDate) throws SQLException;
    void close();
}
