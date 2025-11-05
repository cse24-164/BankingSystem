package com.example.bankaccount;

import java.util.List;

public interface CustomerDAO {
    void saveCustomer(Customer customer);
    Customer findCustomerById(String id);
    List<Customer> findAllCustomers();
    void updateCustomer(Customer customer);
    void deleteCustomer(String id);
    boolean customerExists(String id);
}
