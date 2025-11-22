package com.example.bankaccount;

public class SavingsAccount extends com.example.bankaccount.Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005; // 0.05%

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
    public void applyInterest() {
        double interest = getBalance() * INTEREST_RATE;
        if (interest > 0) {
            addInterestTransaction(interest, "Monthly savings interest at 0.05%");
        }
    }

    @Override
    public void showAccountType() {
        System.out.println("Savings Account [" + getAccountNumber() + "]");
    }

    public double getInterestRate() {
        return INTEREST_RATE;
    }
}
