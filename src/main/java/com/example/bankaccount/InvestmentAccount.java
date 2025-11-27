package com.example.bankaccount;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class InvestmentAccount extends Account implements InterestBearing, Withdrawable {
    private static final double MINIMUM_DEPOSIT = 500.0;
    private static final double INTEREST_RATE = 0.05; // 5%
    private final LocalDate maturityDate;
    private Date lastInterestDate;
    private JDBCTransactionDAO transactionDAO;

    public InvestmentAccount(String branch, Customer customer, double initialDeposit) {
        super(branch, customer, "INVESTMENT");
        this.maturityDate = LocalDate.now().plusYears(1);
        if (initialDeposit > 0) {
            deposit(initialDeposit);
        }
    }

    public InvestmentAccount(String branch, Customer customer, JDBCTransactionDAO transactionDAO) {
        super(branch, customer, "INVESTMENT");
        this.maturityDate = LocalDate.now().plusYears(1);
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
        if (monthsElapsed < 1) return false; // no interest due

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
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }

        if (getBalance() >= amount) {
            updateBalance(getBalance() - amount);
            Transaction transaction = new Transaction(
                    getAccountNumber(),
                    "WITHDRAWAL",
                    amount,
                    getBalance(),
                    "Investment account withdrawal"
            );
            getTransactionHistory().add(transaction);
        } else {
            System.out.println("Insufficient balance for withdrawal.");
        }
    }

    @Override
    public void showAccountType() {
        System.out.println("Investment Account [" + getAccountNumber() + "]");
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
