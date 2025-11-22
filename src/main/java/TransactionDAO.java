package com.example.bankaccount;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface TransactionDAO {
    void saveTransaction(Transaction transaction) throws SQLException;
    List<Transaction> findTransactionsByAccount(String accountNumber) throws SQLException;
    public List<Transaction> getRecentCustomerTransactions(String accountNumber, String accountType);

    void close();
}
