package com.example.bankaccount;
import com.example.bankaccount.Account;
import java.util.List;

public interface AccountDAO {
    void saveAccount(Account account);
    List<Account> getAccountsForCustomer(int customerId);
    Account findAccountByNumber(String accountNumber);
    List<Account> findAllAccounts();
    List<Account> findAccountsByCustomer(int customerId);
    void updateAccount(Account account);
    void deleteAccount(String accountNumber);
    boolean accountExists(String accountNumber);
    List<Account> getRecentlyCreatedAccounts();
}
