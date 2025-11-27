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

        scheduler.scheduleAtFixedRate(this::applyInterestToAllAccounts, 0, 24, TimeUnit.HOURS);
    }


    public void stop() {
        scheduler.shutdown();
    }

    private void applyInterestToAllAccounts() {
        try {
            System.out.println("[InterestScheduler] Checking accounts for due interest...");

            List<Account> allAccounts = bankingService.getAllAccounts();
            for (Account acc : allAccounts) {
                if (acc instanceof InterestBearing interestAcc) {
                    boolean applied = interestAcc.applyInterestIfDue();
                    if (applied) {

                        bankingService.updateAccount(acc);
                    }
                }
            }

            System.out.println("[InterestScheduler] Interest check completed.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[InterestScheduler] ERROR — Scheduler crashed!");
        }
    }
}

