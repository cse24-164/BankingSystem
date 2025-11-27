package com.example.bankaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.sql.SQLException;


public class SavingsAccount extends Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005; // 0.05%
    private Date lastInterestDate;
    private JDBCTransactionDAO transactionDAO;

    public SavingsAccount(String branch, Customer customer) {
        super(branch, customer, "SAVINGS");
    }

    public SavingsAccount(String branch, Customer customer, double initialDeposit) {
        super(branch, customer, "SAVINGS");
        if (initialDeposit > 0) {
            deposit(initialDeposit);
        }
    }

    public SavingsAccount(String branch, Customer customer, JDBCTransactionDAO transactionDAO) {
        super(branch, customer, "SAVINGS");
        this.transactionDAO = transactionDAO;
    }

    @Override
    public boolean applyInterestIfDue() {
        if (lastInterestDate == null) {
            lastInterestDate = new Date(); // initialize to account creation date
        }

        LocalDate lastAppliedDate = lastInterestDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now();

        long monthsElapsed = ChronoUnit.MONTHS.between(lastAppliedDate, today);
        if (monthsElapsed < 1) return false;

        double interestAmount = getBalance() * getInterestRate() * monthsElapsed;

        setBalance(getBalance() + interestAmount);
        addInterestTransaction(interestAmount, "Monthly Interest", transactionDAO);

        lastInterestDate = java.sql.Date.valueOf(today);
        return true;
    }

    protected void addInterestTransaction(double interestAmount, String description, JDBCTransactionDAO transactionDAO) {
        Transaction transaction = new Transaction(getAccountNumber(), "INTEREST", interestAmount, getBalance(), description);

        getTransactionHistory().add(transaction);

        try {
            transactionDAO.saveTransaction(transaction);
        } catch (SQLException e) {
            System.err.println("Failed to save interest transaction for account " + getAccountNumber());
            e.printStackTrace();
        }
    }


    @Override
    public void showAccountType() {
        System.out.println("Savings Account [" + getAccountNumber() + "]");
    }

    public double getInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public void setLastInterestDate(Date lastInterestDate) {
        this.lastInterestDate = lastInterestDate;
    }

    @Override
    public Date getLastInterestDate() {return lastInterestDate;}
}
