package com.example.bankaccount;

public class ChequeAccount extends Account implements Withdrawal {
    private EmploymentInfo employmentInfo;

    public ChequeAccount(String branch, Customer customer, EmploymentInfo employmentInfo) {
        super(branch, customer);
        this.employmentInfo = employmentInfo;
    }

    @Override
    public void showAccountType() {
        System.out.println("Cheque Account [" + getAccountNumber() + "] - Employer: " +
                employmentInfo.getEmployerName());
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && getBalance() >= amount) {
            updateBalance(getBalance() - amount);
            System.out.println("Withdrew " + amount + " from cheque account " + getAccountNumber());

            Transaction transaction = new Transaction(getAccountNumber(), "WITHDRAWAL", amount,
                    getBalance(), "Cheque account withdrawal");
            getTransactionHistory().add(transaction);
        } else {
            System.out.println("Not enough balance or invalid amount for cheque account.");
        }
    }

    @Override
    public double getBalance() {
        return super.getBalance();
    }

    public EmploymentInfo getEmploymentInfo() {
        return employmentInfo;
    }
}