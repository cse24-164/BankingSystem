package com.example.bankaccount;

import java.util.List;

public interface CustomerDAO {
    void saveCustomer(Customer customer);
    Customer findCustomerById(int id);
    List<Customer> findAllCustomers();
    void updateCustomer(Customer customer);
    void deleteCustomer(int id);
    boolean customerExists(int id);
    List<Individual> getAllIndividuals();
    List<Company> getAllCompanies();

}
