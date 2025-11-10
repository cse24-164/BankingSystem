package com.example.bankaccount;

public class ChequeAccount extends com.example.bankaccount.Account implements com.example.bankaccount.Withdrawable {

    public ChequeAccount(String branch, Customer customer, double initialDeposit) {
        super(branch, customer);

        if (!(customer instanceof Individual)) {
            throw new IllegalArgumentException("Cheque accounts are only for individual customers.");
        }

        Individual individual = (Individual) customer;

        if (!individual.hasVerifiedIncome()) {
            throw new IllegalArgumentException("Cheque account requires a customer with valid employment income.");
        }

        // Validate initial deposit
        if (initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative.");
        }

        if (initialDeposit > 0) {
            this.deposit(initialDeposit);
        }
    }
    public ChequeAccount(String branch, Customer customer) {
        this(branch, customer, 0.0);
    }


    @Override
    public void showAccountType() {
        System.out.println("Cheque Account [" + getAccountNumber() + "]"); // ✅ semicolon fixed
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            return;
        }

        double currentBalance = getBalance();

        if (currentBalance >= amount) {
            updateBalance(currentBalance - amount);

            String employerName = "";
            Customer customer = getCustomer();
            if (customer instanceof Individual) {
                Individual individual = (Individual) customer;
                if (individual.getEmployerName() != null && !individual.getEmployerName().isEmpty()) {
                    employerName = individual.getEmployerName();
                }
            }

            Transaction transaction = new Transaction(
                    getAccountNumber(),
                    "WITHDRAWAL",
                    amount,
                    getBalance(),
                    "Cheque account withdrawal - Employer: " + employerName
            );

            getTransactionHistory().add(transaction);
        } else {
            System.out.println("Insufficient funds for withdrawal.");
        }
    }

}
