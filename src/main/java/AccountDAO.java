package com.example.bankaccount;

import java.util.List;

public interface AccountDAO {
    void saveAccount(Account account);
    Account findAccountByNumber(int accountNumber);
    List<Account> findAllAccounts();
    List<Account> findAccountsByCustomer(String customerId);
    void updateAccount(Account account);
    void deleteAccount(int accountNumber);
    boolean accountExists(int accountNumber);
    List<Account> getRecentlyCreatedAccounts(int limit);
}