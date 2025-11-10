package com.example.bankaccount;

public class InvestmentAccount extends com.example.bankaccount.Account
        implements com.example.bankaccount.InterestBearing, com.example.bankaccount.Withdrawable {
    private static final double MINIMUM_DEPOSIT = 500.0;
    private static final double INTEREST_RATE = 0.05; // 5%

    public InvestmentAccount(String branch, Customer customer, double initialDeposit) {
        super(branch, customer);

        if (initialDeposit < MINIMUM_DEPOSIT) {
            throw new IllegalArgumentException("Minimum opening deposit is " + MINIMUM_DEPOSIT);
        }

        deposit(initialDeposit);
    }

    @Override
    public void applyInterest() {
        double interest = calculateInterest();
        if (interest > 0) {
            addInterestTransaction(interest, "Monthly investment interest at 5%");
        }
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
}
