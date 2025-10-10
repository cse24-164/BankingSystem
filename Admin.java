package com.example.bankaccount;

public class Admin extends User {

    public Admin(String username, String password) {
        super(username, password, "ADMIN");
    }

    public Account openAccount(Customer customer, String accountType, String branch) {
        Account newAccount = null;

        if (accountType.equalsIgnoreCase("savings")) {
            newAccount = new SavingsAccount(branch, customer);
        } else if (accountType.equalsIgnoreCase("cheque")) {
            if (customer instanceof Individual) {
                Individual individual = (Individual) customer;
                if (individual.isEmployed()) {
                    EmploymentInfo empInfo = individual.getEmploymentInfo();
                    newAccount = new ChequeAccount(branch, customer, empInfo);
                } else {
                    System.out.println("Cannot open cheque account - customer not employed");
                    return null;
                }
            } else {
                System.out.println("Cheque accounts only available for individual customers");
                return null;
            }
        } else if (accountType.equalsIgnoreCase("investment")) {
            newAccount = new InvestmentAccount(branch, customer, 500.0);
        } else {
            System.out.println("Invalid account type: " + accountType);
            return null;
        }

        customer.getAccounts().add(newAccount);
        System.out.println("Admin " + getUsername() + " opened new " + accountType +
                " account (#" + newAccount.getAccountNumber() + ") for customer " +
                customer.getFirstName() + " " + customer.getSurname());
        return newAccount;
    }
}