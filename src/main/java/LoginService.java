package com.example.bankaccount;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LoginService {
    private BankingService bankingService;
    private Random random;

    // In-memory credential storage (replace with database in production)
    private Map<String, CredentialInfo> credentialsByUsername = new ConcurrentHashMap<>();
    private Map<String, CredentialInfo> credentialsByCustomerId = new ConcurrentHashMap<>();

    public LoginService() {
        this.bankingService = new BankingService();
        this.random = new Random();
    }

    // Inner class for credential information
    private static class CredentialInfo {
        private String username;
        private String password;
        private String customerId;
        private Date createdDate;
        private boolean temporaryPassword;

        public CredentialInfo(String username, String password, String customerId) {
            this.username = username;
            this.password = password;
            this.customerId = customerId;
            this.createdDate = new Date();
            this.temporaryPassword = true;
        }

        // Getters and setters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getCustomerId() { return customerId; }
        public Date getCreatedDate() { return createdDate; }
        public boolean isTemporaryPassword() { return temporaryPassword; }
        public void setPassword(String password) { this.password = password; }
        public void setTemporaryPassword(boolean temporary) { this.temporaryPassword = temporary; }
    }

    public AuthContext login(String username, String password) {
        // Teller login (hardcoded for demo)
        if (authenticateTeller(username, password)) {
            BankTeller teller = new BankTeller("May", "Sab", "EMP123", "BRANCH01");
            return new AuthContext(username, "BANK_TELLER", teller);
        }

        // Customer login
        if (validateCustomerCredentials(username, password)) {
            CredentialInfo credentials = credentialsByUsername.get(username.toLowerCase());
            Customer customer = bankingService.getCustomerById(credentials.getCustomerId());
            if (customer != null) {
                return new AuthContext(username, "CUSTOMER", customer);
            }
        }

        return null;
    }

    private boolean authenticateTeller(String username, String password) {
        // In production, this would query a teller database
        Map<String, String> tellerCredentials = Map.of(
                "teller1", "pass1",
                "teller2", "pass2",
                "admin", "admin123"
        );
        return tellerCredentials.containsKey(username) &&
                tellerCredentials.get(username).equals(password);
    }

    private boolean validateCustomerCredentials(String username, String password) {
        CredentialInfo credentials = credentialsByUsername.get(username.toLowerCase());
        return credentials != null && credentials.getPassword().equals(password);
    }

    public CredentialInfo setupCustomerCredentials(Customer customer) {
        String username = generateConsistentUsername(customer);
        String password = generateSecurePassword();
        String customerId = customer.getIdentificationNumber();

        CredentialInfo credentials = new CredentialInfo(username, password, customerId);

        // Store in memory
        credentialsByUsername.put(username.toLowerCase(), credentials);
        credentialsByCustomerId.put(customerId, credentials);

        // Also set on customer object for file storage
        customer.setUsername(username);
        customer.setPassword(password);

        return credentials;
    }

    public String generateConsistentUsername(Customer customer) {
        String baseUsername = getUsernameBase(customer);
        String id = customer.getIdentificationNumber();

        // Consistent digits based on customer ID
        int consistentDigits = Math.abs(id.hashCode() % 900) + 100;
        return baseUsername + consistentDigits;
    }

    private String generateUniqueUsername(Customer customer) {
        String baseUsername = generateConsistentUsername(customer);

        int counter = 1;
        String finalUsername = baseUsername;
        while (credentialsByUsername.containsKey(finalUsername.toLowerCase()) && counter < 100) {
            finalUsername = baseUsername + counter;
            counter++;
        }

        return finalUsername;
    }

    private String getUsernameBase(Customer customer) {
        if (customer instanceof Individual) {
            Individual individual = (Individual) customer;
            return individual.getFirstName().toLowerCase().replaceAll("\\s+", "");
        } else if (customer instanceof Company) {
            Company company = (Company) customer;
            String base = company.getCompanyName().toLowerCase()
                    .replaceAll("\\s+", "")
                    .replaceAll("[^a-z0-9]", "");
            return base.length() > 8 ? base.substring(0, 8) : base;
        }
        return "customer";
    }

    public String generateSecurePassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";

        StringBuilder password = new StringBuilder();

        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));

        String allChars = uppercase + lowercase + numbers;
        for (int i = 3; i < 8; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        CredentialInfo credentials = credentialsByUsername.get(username.toLowerCase());
        if (credentials != null && credentials.getPassword().equals(oldPassword)) {
            credentials.setPassword(newPassword);
            credentials.setTemporaryPassword(false);

            updateCustomerPassword(credentials.getCustomerId(), newPassword);
            return true;
        }
        return false;
    }

    private void updateCustomerPassword(String customerId, String newPassword) {
        try {
            Customer customer = bankingService.getCustomerById(customerId);
            if (customer != null) {
                customer.setPassword(newPassword);
                bankingService.updateCustomer(customer);
            }
        } catch (Exception e) {
            System.err.println("Failed to update customer password: " + e.getMessage());
        }
    }

    public void resetPassword(String customerId) {
        CredentialInfo credentials = credentialsByCustomerId.get(customerId);
        if (credentials != null) {
            String newPassword = generateSecurePassword();
            credentials.setPassword(newPassword);
            credentials.setTemporaryPassword(true);
            updateCustomerPassword(customerId, newPassword);
        }
    }

    public boolean usernameExists(String username) {
        return credentialsByUsername.containsKey(username.toLowerCase());
    }

    public List<String> getAllUsernames() {
        return new ArrayList<>(credentialsByUsername.keySet());
    }

    public CredentialInfo getCredentialInfo(String username) {
        return credentialsByUsername.get(username.toLowerCase());
    }
}