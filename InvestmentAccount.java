package com.example.bankaccount;

public class InvestmentAccount extends Account implements InterestBearing, Withdrawal {
    private static final double MINIMUM_DEPOSIT = 500.0;
    private static final double INTEREST_RATE = 0.05; // 5%

    public InvestmentAccount(String branch, Customer customer, double initialDeposit) {
        super(branch, customer);
        if (initialDeposit >= MINIMUM_DEPOSIT) {
            deposit(initialDeposit);
        } else {
            throw new IllegalArgumentException("Minimum opening deposit is " + MINIMUM_DEPOSIT);
        }
    }

    // Interface impleentation
    @Override
    public void applyInterest() {
        double interest = calculateInterest();
        if (interest > 0) {
            addInterestTransaction(interest, "Monthly investment interest at 5%");
            System.out.println("Applied interest of " + interest + " to investment account " + getAccountNumber());
        }
    }

    // Helper method to calculate interest
    private double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }

    // Withdrawal interface implementation
    @Override
    public void withdraw(double amount) {
        if (amount > 0 && getBalance() >= amount) {
            updateBalance(getBalance() - amount);
            System.out.println("Withdrew " + amount + " from investment account " + getAccountNumber());

            Transaction transaction = new Transaction(getAccountNumber(), "WITHDRAWAL", amount,
                    getBalance(), "Investment account withdrawal");
            getTransactionHistory().add(transaction);
        } else {
            System.out.println("Not enough balance or invalid amount for investment account.");
        }
    }

    @Override
    public double getBalance() {
        return super.getBalance();
    }

    @Override
    public void showAccountType() {
        System.out.println("Investment Account [" + getAccountNumber() + "]");
    }
    public double getInterestRate() {
        return INTEREST_RATE;
    }


}
