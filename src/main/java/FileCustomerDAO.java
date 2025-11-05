package com.example.bankaccount;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileCustomerDAO implements CustomerDAO {
    private static final String CUSTOMER_FILE = "customers.txt";
    private List<Customer> customersList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FileCustomerDAO() {
        loadCustomersFromFile();
    }

    @Override
    public void saveCustomer(Customer customer) {
        // Check if customer already exists
        Customer existing = findCustomerById(customer.getIdentificationNumber());
        if (existing != null) {
            // Update existing customer
            customersList.remove(existing);
        }
        customersList.add(customer);
        saveCustomersToFile();
        System.out.println("✅ Customer saved: " + customer.getDisplayName() + " (" + customer.getIdentificationNumber() + ")");
    }

    @Override
    public Customer findCustomerById(String id) {
        for (Customer customer : customersList) {
            if (customer.getIdentificationNumber().equals(id)) {
                System.out.println("✅ Customer found: " + customer.getDisplayName() + " (" + id + ")");
                return customer;
            }
        }
        System.out.println("❌ Customer not found: " + id);
        return null;
    }

    @Override
    public void updateCustomer(Customer customer) {
        Customer existing = findCustomerById(customer.getIdentificationNumber());
        if (existing != null) {
            customersList.remove(existing);
            customersList.add(customer);
            saveCustomersToFile();
            System.out.println("✅ Customer updated: " + customer.getDisplayName());
        } else {
            System.out.println("❌ Customer not found for update: " + customer.getIdentificationNumber());
        }
    }

    @Override
    public void deleteCustomer(String id) {
        Customer customer = findCustomerById(id);
        if (customer != null) {
            customersList.remove(customer);
            saveCustomersToFile();
            System.out.println("✅ Customer deleted: " + customer.getDisplayName() + " (" + id + ")");
        } else {
            System.out.println("❌ Customer not found for deletion: " + id);
        }
    }

    @Override
    public boolean customerExists(String id) {
        boolean exists = findCustomerById(id) != null;
        System.out.println("🔍 Customer exists (" + id + "): " + exists);
        return exists;
    }

    private void loadCustomersFromFile() {
        File file = new File(CUSTOMER_FILE);
        if (!file.exists()) {
            System.out.println("📁 No existing customer file found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CUSTOMER_FILE))) {
            String line;
            int loadedCount = 0;

            while ((line = reader.readLine()) != null) {
                Customer customer = parseCustomer(line);
                if (customer != null) {
                    customersList.add(customer);
                    loadedCount++;
                }
            }

            System.out.println("✅ Loaded " + loadedCount + " customers from file");

        } catch (IOException e) {
            System.err.println("❌ Error reading customers file: " + e.getMessage());
        }
    }

    private void saveCustomersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CUSTOMER_FILE))) {
            for (Customer customer : customersList) {
                writer.println(formatCustomer(customer));
            }
            System.out.println("💾 Saved " + customersList.size() + " customers to file");
        } catch (IOException e) {
            System.err.println("❌ Error saving customers to file: " + e.getMessage());
        }
    }

    private Customer parseCustomer(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 8) {
                System.err.println("❌ Invalid customer line (too few parts): " + line);
                return null;
            }

            String type = parts[0];

            if ("INDIVIDUAL".equals(type)) {
                return parseIndividual(parts);
            } else if ("COMPANY".equals(type)) {
                return parseCompany(parts);
            } else {
                System.err.println("❌ Unknown customer type: " + type);
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Error parsing customer line: " + line);
            System.err.println("   Error: " + e.getMessage());
            return null;
        }
    }

    private Individual parseIndividual(String[] parts) {
        try {
            // Updated format: TYPE|firstName|surname|address|idNumber|dob|gender|email|phone|username|password|nokFirstName|nokSurname|nokRelationship|nokGender|nokPhone|nokEmail|nokAddress
            String firstName = parts[1];
            String surname = parts[2];
            String address = parts[3];
            String idNumber = parts[4];
            Date dob = dateFormat.parse(parts[5]);
            String gender = parts[6];
            String email = parts[7];
            String phone = parts[8];
            String username = parts.length > 9 ? parts[9] : null; // ADD USERNAME
            String password = parts.length > 10 ? parts[10] : null; // ADD PASSWORD

            // Parse NextOfKin if available
            NextOfKin nextOfKin = null;
            if (parts.length >= 17) {  // Adjusted for new fields
                nextOfKin = new NextOfKin(
                        parts[11],   // firstName (moved from 9)
                        parts[12],   // surname (moved from 10)
                        parts[13],   // relationship (moved from 11)
                        parts[14],   // nokGender (moved from 12)
                        parts[15],   // phone (moved from 13)
                        parts[16],   // email (moved from 14)
                        parts.length > 17 ? parts[17] : address // address (moved from 15)
                );
            }

            Individual individual = new Individual(firstName, surname, address, idNumber,
                    dob, gender, email, phone, nextOfKin);

            // SET USERNAME AND PASSWORD
            individual.setUsername(username);
            individual.setPassword(password);

            System.out.println("✅ Parsed individual: " + individual.getFullName() + " | Username: " + username);
            return individual;

        } catch (Exception e) {
            System.err.println("❌ Error parsing individual: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Company parseCompany(String[] parts) {
        try {
            // Updated format: TYPE|companyName|registrationNumber|businessType|address|email|phone|contactPerson|annualRevenue|username|password
            String companyName = parts[1];
            String registrationNumber = parts[2];
            String businessType = parts[3];
            String address = parts[4];
            String email = parts[5];
            String phone = parts[6];
            String contactPerson = parts[7];
            double annualRevenue = Double.parseDouble(parts[8]);
            String username = parts.length > 9 ? parts[9] : null; // ADD USERNAME
            String password = parts.length > 10 ? parts[10] : null; // ADD PASSWORD

            Company company = new Company(companyName, registrationNumber, businessType, address, email, phone, contactPerson, annualRevenue);

            // SET USERNAME AND PASSWORD
            company.setUsername(username);
            company.setPassword(password);

            System.out.println("✅ Parsed company: " + company.getDisplayName() + " | Username: " + username);
            return company;

        } catch (Exception e) {
            System.err.println("❌ Error parsing company: " + e.getMessage());
            return null;
        }
    }

    private String formatCustomer(Customer customer) {
        StringBuilder sb = new StringBuilder();

        if (customer instanceof Individual) {
            Individual individual = (Individual) customer;
            sb.append("INDIVIDUAL|")
                    .append(individual.getFirstName()).append("|")
                    .append(individual.getSurname()).append("|")
                    .append(individual.getAddress()).append("|")
                    .append(individual.getIdNumber()).append("|")
                    .append(dateFormat.format(individual.getDateOfBirth())).append("|")
                    .append(individual.getGender()).append("|")
                    .append(individual.getContactEmail()).append("|")
                    .append(individual.getContactPhone()).append("|")
                    .append(individual.getUsername() != null ? individual.getUsername() : "").append("|") // ADD USERNAME
                    .append(individual.getPassword() != null ? individual.getPassword() : ""); // ADD PASSWORD

            NextOfKin nextOfKin = individual.getNextOfKin();
            if (nextOfKin != null) {
                sb.append("|").append(nextOfKin.getFirstName())
                        .append("|").append(nextOfKin.getSurname())
                        .append("|").append(nextOfKin.getRelationship())
                        .append("|").append(nextOfKin.getNokGender())
                        .append("|").append(nextOfKin.getPhoneNumber())
                        .append("|").append(nextOfKin.getEmail())
                        .append("|").append(nextOfKin.getAddress());
            }

        } else if (customer instanceof Company) {
            Company company = (Company) customer;
            sb.append("COMPANY|")
                    .append(company.getCompanyName()).append("|")
                    .append(company.getRegistrationNumber()).append("|")
                    .append(company.getBusinessType()).append("|")
                    .append(company.getAddress()).append("|")
                    .append(company.getContactEmail()).append("|")
                    .append(company.getContactPhone()).append("|")
                    .append(company.getContactPersonName()).append("|")
                    .append(company.getAnnualRevenue()).append("|")
                    .append(company.getUsername() != null ? company.getUsername() : "").append("|") // ADD USERNAME
                    .append(company.getPassword() != null ? company.getPassword() : ""); // ADD PASSWORD
        }

        return sb.toString();
    }

    @Override
    public List<Customer> findAllCustomers() {
        System.out.println("📋 Retrieving all " + customersList.size() + " customers from cache");
        return new ArrayList<>(customersList); // Return a copy
    }
}