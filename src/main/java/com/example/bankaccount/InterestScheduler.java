package com.example.bankaccount;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InterestScheduler {

    private final BankingService bankingService;
    private final ScheduledExecutorService scheduler;

    public InterestScheduler(BankingService bankingService) {
        this.bankingService = bankingService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // Start the scheduler
    public void start() {
        // Initial delay 0, then run every 24 hours
        scheduler.scheduleAtFixedRate(this::applyInterestToAllAccounts, 0, 24, TimeUnit.HOURS);
    }

    // Stop the scheduler if needed
    public void stop() {
        scheduler.shutdown();
    }

    private void applyInterestToAllAccounts() {
        System.out.println("[InterestScheduler] Checking accounts for due interest...");

        List<Account> allAccounts = bankingService.getAllAccounts();

        for (Account acc : allAccounts) {
            if (acc instanceof InterestBearing interestAcc) {
                // Use customer's registration date for first interest calculation
                Date customerRegDate = acc.getCustomer().getRegistrationDate();
                interestAcc.applyInterestIfDue(customerRegDate);

                // Save the updated account back to DB
                bankingService.updateAccount(acc);
            }
        }

        System.out.println("[InterestScheduler] Interest check completed.");
    }
}

