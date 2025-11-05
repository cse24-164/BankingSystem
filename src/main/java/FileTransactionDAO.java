package com.example.bankaccount;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileTransactionDAO implements TransactionDAO {
    private static final String TRANSACTION_FILE = "transactions.txt";
    private List<Transaction> transactionsList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FileTransactionDAO() {
        loadTransactionsFromFile();
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionsList.add(transaction);
        saveTransactionsToFile();
        System.out.println("✅ Transaction saved: " + transaction.getTransactionType() + " for account #" + transaction.getAccountNumber());
    }

    @Override
    public List<Transaction> findTransactionsByAccount(int accountNumber) {
        List<Transaction> accountTransactions = new ArrayList<>();
        for (Transaction transaction : transactionsList) {
            if (transaction.getAccountNumber() == accountNumber) {
                accountTransactions.add(transaction);
            }
        }
        System.out.println("📋 Found " + accountTransactions.size() + " transactions for account #" + accountNumber);
        return accountTransactions;
    }

    @Override
    public List<Transaction> findAllTransactions() {
        System.out.println("📋 Retrieving all " + transactionsList.size() + " transactions");
        return new ArrayList<>(transactionsList);
    }

    @Override
    public void deleteTransactionsByAccount(int accountNumber) {
        List<Transaction> toRemove = new ArrayList<>();
        for (Transaction transaction : transactionsList) {
            if (transaction.getAccountNumber() == accountNumber) {
                toRemove.add(transaction);
            }
        }
        transactionsList.removeAll(toRemove);
        saveTransactionsToFile();
        System.out.println("✅ Deleted " + toRemove.size() + " transactions for account #" + accountNumber);
    }

    private void loadTransactionsFromFile() {
        File file = new File(TRANSACTION_FILE);
        if (!file.exists()) {
            System.out.println("📁 No existing transactions file found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line;
            int loadedCount = 0;

            while ((line = reader.readLine()) != null) {
                Transaction transaction = parseTransaction(line);
                if (transaction != null) {
                    transactionsList.add(transaction);
                    loadedCount++;
                }
            }

            System.out.println("✅ Loaded " + loadedCount + " transactions from file");

        } catch (IOException e) {
            System.err.println("❌ Error reading transactions file: " + e.getMessage());
        }
    }

    private void saveTransactionsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTION_FILE, true))) { // Append mode
            // Only save new transactions (not the entire list every time)
            // This is more efficient for transaction logging
            for (Transaction transaction : transactionsList) {
                writer.println(formatTransaction(transaction));
            }
            System.out.println("💾 Saved " + transactionsList.size() + " transactions to file");
        } catch (IOException e) {
            System.err.println("❌ Error saving transactions to file: " + e.getMessage());
        }
    }

    private Transaction parseTransaction(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 6) {
                System.err.println("❌ Invalid transaction line: " + line);
                return null;
            }

            int accountNumber = Integer.parseInt(parts[0]);
            String transactionType = parts[1];
            double amount = Double.parseDouble(parts[2]);
            double balanceAfter = Double.parseDouble(parts[3]);
            String description = parts[4];
            Date timestamp = dateFormat.parse(parts[5]);

            Transaction transaction = new Transaction(accountNumber, transactionType, amount, balanceAfter, description);
            transaction.setTransactionDate(timestamp);

            System.out.println("✅ Parsed transaction: " + transactionType + " for account #" + accountNumber);
            return transaction;

        } catch (Exception e) {
            System.err.println("❌ Error parsing transaction line: " + line);
            return null;
        }
    }

    private String formatTransaction(Transaction transaction) {
        return transaction.getAccountNumber() + "|" +
                transaction.getTransactionType() + "|" +
                transaction.getAmount() + "|" +
                transaction.getBalance() + "|" +
                transaction.getDescription() + "|" +
                dateFormat.format(transaction.getTransactionDate());
    }
}
