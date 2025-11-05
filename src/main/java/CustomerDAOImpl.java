package com.example.bankaccount;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;

public class CustomerDAOImpl implements CustomerDAO {
    private List<Customer> customers = new ArrayList<>();
    private final String DATA_FILE = "customers.txt";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public CustomerDAOImpl() {
        loadCustomersFromFile();
    }

    @Override
    public void saveCustomer(Customer customer) {
        if (!customerExists(customer.getIdentificationNumber())) {
            customers.add(customer);
            saveCustomersToFile();
        }
    }

    @Override
    public Customer findCustomerById(String id) {
        return customers.stream()
                .filter(c -> c.getIdentificationNumber().equals(id))
                .findFirst()
                .orElse(null);
    }


    public Customer findCustomerByUsername(String username) {
        return customers.stream()
                .filter(c -> c.getUsername() != null && c.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Customer> findAllCustomers() {
        return new ArrayList<>(customers);
    }

    @Override
    public void updateCustomer(Customer updatedCustomer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getIdentificationNumber().equals(updatedCustomer.getIdentificationNumber())) {
                customers.set(i, updatedCustomer);
                saveCustomersToFile();
                return;
            }
        }
    }

    @Override
    public void deleteCustomer(String id) {
        customers.removeIf(c -> c.getIdentificationNumber().equals(id));
        saveCustomersToFile();
    }

    @Override
    public boolean customerExists(String id) {
        return customers.stream().anyMatch(c -> c.getIdentificationNumber().equals(id));
    }

    // ✅ UPDATED FILE STORAGE FOR BOTH CUSTOMER TYPES

    private void loadCustomersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Customer customer = parseCustomer(line);
                if (customer != null) {
                    customers.add(customer);
                }
            }
            System.out.println("✅ Loaded " + customers.size() + " customers from file");
        } catch (FileNotFoundException e) {
            System.out.println("ℹ️ No existing customer file found. Starting fresh.");
        } catch (IOException e) {
            System.out.println("❌ Error loading customers: " + e.getMessage());
        }
    }

    private void saveCustomersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Customer customer : customers) {
                writer.write(customerToFileString(customer));
                writer.newLine();
            }
            System.out.println("💾 Saved " + customers.size() + " customers to file");
        } catch (IOException e) {
            System.out.println("❌ Error saving customers: " + e.getMessage());
        }
    }

    private Customer parseCustomer(String line) {
        try {
            String[] parts = line.split("\\|");
            String type = parts[0];

            switch (type) {
                case "INDIVIDUAL":
                    return parseIndividual(parts);
                case "COMPANY":
                    return parseCompany(parts);
                default:
                    return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error parsing customer: " + line);
            return null;
        }
    }

    private Individual parseIndividual(String[] parts) {
        try {
            // Updated format: TYPE|firstName|surname|address|idNumber|dob|gender|email|phone|nokFirstName|nokSurname|nokRelationship|nokGender|nokPhone|nokEmail|nokAddress
            String firstName = parts[1];
            String surname = parts[2];
            String address = parts[3];
            String idNumber = parts[4];
            Date dob = dateFormat.parse(parts[5]);
            String gender = parts[6];
            String email = parts[7];
            String phone = parts[8];

            // Parse NextOfKin if available
            NextOfKin nextOfKin = null;
            if (parts.length >= 15) {  // Changed from 14 to 15 for 7 parameters
                nextOfKin = new NextOfKin(
                        parts[9],   // firstName
                        parts[10],  // surname
                        parts[11],  // relationship
                        parts[12],  // nokGender (NEW PARAMETER)
                        parts[13],  // phone (moved from 12 to 13)
                        parts[14],  // email (moved from 13 to 14)
                        parts.length > 15 ? parts[15] : address // address (moved from 14 to 15)
                );
            }

            Individual individual = new Individual(firstName, surname, address, idNumber,
                    dob, gender, email, phone, nextOfKin);

            System.out.println("✅ Parsed individual: " + individual.getFullName());
            return individual;

        } catch (Exception e) {
            System.err.println("❌ Error parsing individual: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Company parseCompany(String[] parts) {
        // Format: TYPE|registrationNumber|companyName|username|password|address|email|phone|businessType|contactPerson|revenue
        String regNumber = parts[1];
        String companyName = parts[2];
        String username = parts[3];
        String password = parts[4];
        String address = parts[5];
        String email = parts[6];
        String phone = parts[7];
        String businessType = parts[8];
        String contactPerson = parts[9];
        double revenue = Double.parseDouble(parts[10]);

        Company company = new Company(companyName, regNumber, businessType, address,
                email, phone, contactPerson, revenue);
        company.setUsername(username);
        company.setPassword(password);
        return company;
    }

    private String customerToFileString(Customer customer) {
        if (customer instanceof Individual) {
            return individualToFileString((Individual) customer);
        } else if (customer instanceof Company) {
            return companyToFileString((Company) customer);
        }
        return "";
    }

    private String individualToFileString(Individual individual) {
        return String.join("|",
                "INDIVIDUAL",
                individual.getIdentificationNumber(), // idNumber
                individual.getFirstName(),
                individual.getSurname(),
                individual.getUsername(),
                individual.getPassword(),
                individual.getAddress(),
                individual.getContactEmail(),
                individual.getContactPhone(),
                individual.getIdNumber(),
                dateFormat.format(individual.getDateOfBirth())
        );
    }

    private String companyToFileString(Company company) {
        return String.join("|",
                "COMPANY",
                company.getIdentificationNumber(), // registrationNumber
                company.getCompanyName(),
                company.getUsername(),
                company.getPassword(),
                company.getAddress(),
                company.getContactEmail(),
                company.getContactPhone(),
                company.getBusinessType(),
                company.getContactPersonName(),
                String.valueOf(company.getAnnualRevenue())
        );
    }
}