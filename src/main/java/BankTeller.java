package com.example.bankaccount;

public class BankTeller extends User{
    private String tellerId;
    private String firstName;
    private String lastName;
    private String employeeId;
    private String branchCode;
    private BankingService bankingService;

    public BankTeller(String username, String password, String firstName, String lastName, String employeeId, String branchCode) {
        super(username, password, "BankTeller");
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeId = employeeId;
        this.branchCode = branchCode;
        this.tellerId = "TEL_" + employeeId;
        this.bankingService = new BankingService();
    }

    // Getters
    public String getTellerId() { return tellerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmployeeId() { return employeeId; }
    public String getBranchCode() { return branchCode; }
    public BankingService getBankingService() { return bankingService; }


    public Account openAccount(Customer customer, String accountType, String branch, double initialDeposit) {
        try {
            System.out.println("Bank Teller " + getFullName() + " opening " + accountType +
                    " account for " + customer.getDisplayName());

            if (!validateAccountRequirements(customer, accountType)) {
                System.out.println(" Account requirements not met for " + accountType + " account");
                return null;
            }

            Account newAccount = bankingService.createAccount(this, customer, accountType, branch, initialDeposit);

            if (newAccount != null) {
                customer.addAccount(newAccount);

                System.out.println("Bank Teller " + getFullName() + " successfully opened " + accountType +
                        " account (#" + newAccount.getAccountNumber() + ") for " + customer.getDisplayName() +
                        " with initial deposit: P" + initialDeposit);
            }

            return newAccount;

        } catch (Exception e) {
            System.err.println("Failed to open account: " + e.getMessage());
            return null;
        }
    }

    public void processDeposit(int accountNumber, double amount, String description) {
        try {
            bankingService.deposit(accountNumber, amount, description);
            System.out.println("Teller " + getFullName() + " processed deposit: P" + amount +
                    " to account #" + accountNumber);
        } catch (Exception e) {
            System.err.println("Deposit failed: " + e.getMessage());
            throw e;
        }
    }

    public double checkBalance(int accountNumber) {
        try {
            double balance = bankingService.getAccountBalance(accountNumber);
            System.out.println("Teller " + getFullName() + " checked balance for account #" +
                    accountNumber + ": P" + balance);
            return balance;
        } catch (Exception e) {
            System.err.println(" Balance check failed: " + e.getMessage());
            throw e;
        }
    }

    public java.util.List<Account> getCustomerAccounts(String customerId) {
        try {
            java.util.List<Account> accounts = bankingService.getCustomerAccounts(customerId);
            System.out.println("Teller " + getFullName() + " retrieved " + accounts.size() +
                    " accounts for customer: " + customerId);
            return accounts;
        } catch (Exception e) {
            System.err.println("Failed to get customer accounts: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }


    public java.util.List<Transaction> getAccountTransactions(int accountNumber) {
        try {
            java.util.List<Transaction> transactions = bankingService.getAccountTransactions(accountNumber);
            System.out.println("📜 Teller " + getFullName() + " retrieved " + transactions.size() +
                    " transactions for account #" + accountNumber);
            return transactions;
        } catch (Exception e) {
            System.err.println("Failed to get transaction history: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    public Customer findCustomer(String customerId) {
        try {
            Customer customer = bankingService.findCustomer(customerId);
            if (customer != null) {
                System.out.println("Teller " + getFullName() + " found customer: " +
                        customer.getDisplayName() + " (" + customerId + ")");
            } else {
                System.out.println("Teller " + getFullName() + " - customer not found: " + customerId);
            }
            return customer;
        } catch (Exception e) {
            System.err.println("Customer search failed: " + e.getMessage());
            return null;
        }
    }

    public boolean canOpenAccountType(Customer customer, String accountType) {
        if (accountType.equalsIgnoreCase("cheque")) {
            if (!(customer instanceof Individual)) {
                System.out.println("Cheque accounts only available for individual customers");
                return false;
            }

            Individual individual = (Individual) customer;
            if (!individual.hasVerifiedIncome()) {
                System.out.println("Cheque account requires verified employment income");
                return false;
            }
            return true;

        } else if (accountType.equalsIgnoreCase("investment")) {

            if (customer instanceof Individual) {
                Individual individual = (Individual) customer;
                if (individual.getMonthlyIncome() < 500.0) {
                    System.out.println("Individual customers need minimum P500 monthly income for investment account");
                    return false;
                }
            }
            return true;
        }

        return true; // Savings accounts are available for all
    }

    private boolean validateAccountRequirements(Customer customer, String accountType) {
        if (accountType.equalsIgnoreCase("cheque")) {
            if (!(customer instanceof Individual)) {
                System.out.println("Cheque accounts only available for individual customers");
                return false;
            }

            Individual individual = (Individual) customer;
            if (!individual.hasVerifiedIncome()) {
                System.out.println("Cheque account requires verified employment income");
                return false;
            }

        } else if (accountType.equalsIgnoreCase("investment")) {
            if (customer instanceof Individual) {
                Individual individual = (Individual) customer;
                if (individual.getMonthlyIncome() < 500.0) {
                    System.out.println("Individuals need minimum P500 monthly income for investment account");
                    return false;
                }
            }
        }

        return true;
    }

    public void applyMonthlyInterest() {
        try {
            bankingService.applyMonthlyInterest();
            System.out.println(" Teller " + getFullName() + " applied monthly interest to all eligible accounts");
        } catch (Exception e) {
            System.err.println(" Failed to apply monthly interest: " + e.getMessage());
        }
    }
}