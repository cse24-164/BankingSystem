package com.example.bankaccount;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class InvestmentAccount extends com.example.bankaccount.Account
        implements com.example.bankaccount.InterestBearing, com.example.bankaccount.Withdrawable {
    private static final double MINIMUM_DEPOSIT = 500.0;
    private static final double INTEREST_RATE = 0.05; // 5%
    private final LocalDate maturityDate;
    private Date lastInterestDate;

    public InvestmentAccount(String branch, Customer customer, double initialDeposit) {
        super(branch, customer, "INVESTMENT");
        this.maturityDate = LocalDate.now().plusYears(1);
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

        double interestRate = 0.05; // monthly interest
        double interestAmount = getBalance() * interestRate * monthsElapsed;

        // Credit interest
        setBalance(getBalance() + interestAmount);
        addInterestTransaction(interestAmount, "Monthly Interest");

        lastInterestDate = new Date(); // update lastInterestDate
    }

    protected void addInterestTransaction(double interestAmount, String description) {
        Transaction transaction = new Transaction(getAccountNumber(), "INTEREST",
                interestAmount, getBalance(), description);
        getTransactionHistory().add(transaction);
    }

    private double calculateInterest() {
        return getBalance() * INTEREST_RATE;
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
