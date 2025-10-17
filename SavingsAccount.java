package com.example.bankaccount;

public class SavingsAccount extends Account implements InterestBearing {
    private static final double INTEREST_RATE = 0.0005; // 0.05%

    public SavingsAccount(String branch, Customer customer) {
        super(branch, customer);
    }

    @Override
    public void withdraw(double amount) {
        System.out.println("Withdrawals not allowed from Savings Account.");
    }

    @Override
    public void applyInterest() {
        double interest = getBalance() * INTEREST_RATE;
        if (interest > 0) {
            addInterestTransaction(interest, "Monthly savings interest at 0.05%");
            System.out.println("Applied interest of " + interest + " to savings account " + getAccountNumber());
        }
    }

    @Override
    public void showAccountType() {
        System.out.println("Savings Account [" + getAccountNumber() + "]");
    }
}
