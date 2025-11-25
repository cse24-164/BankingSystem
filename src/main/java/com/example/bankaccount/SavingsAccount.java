package com.example.bankaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class SavingsAccount extends com.example.bankaccount.Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005; // 0.05%
    private Date lastInterestDate;

    public SavingsAccount(String branch, Customer customer) {
        super(branch, customer, "SAVINGS");
    }

    public SavingsAccount(String branch, Customer customer, double initialDeposit) {
        super(branch, customer, "SAVINGS");
        if (initialDeposit > 0) {
            deposit(initialDeposit);
        }
    }

    @Override
    public void applyInterestIfDue(Date customerRegistrationDate) {
        // Determine date to calculate interest from
        Date fromDate = lastInterestDate != null ? lastInterestDate : customerRegistrationDate;
        LocalDate lastApplied = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        long monthsElapsed = ChronoUnit.MONTHS.between(lastApplied.withDayOfMonth(1),
                today.withDayOfMonth(1));

        if (monthsElapsed <= 0) return; // No interest due

        double interestRate = 0.0005; // monthly interest
        double interestAmount = getBalance() * interestRate * monthsElapsed;

        // Credit interest
        setBalance(getBalance() + interestAmount);
        addInterestTransaction(interestAmount, "Monthly Interest");

        lastInterestDate = new Date();
    }

    protected void addInterestTransaction(double interestAmount, String description) {
        Transaction transaction = new Transaction(getAccountNumber(), "INTEREST",
                interestAmount, getBalance(), description);
        getTransactionHistory().add(transaction);
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
