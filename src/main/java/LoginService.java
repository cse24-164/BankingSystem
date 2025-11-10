package com.example.bankaccount;

import java.sql.*;
import java.util.Random;

public class LoginService {

    private static final String URL = "jdbc:mysql://localhost:3306/bankingdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Random random;

    public LoginService() {
        this.random = new Random();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // LOGIN
    public AuthContext login(String username, String password) {
        try (Connection conn = connect()) {
            // Check if teller login
            if (authenticateTeller(username, password)) {
                BankTeller teller = new BankTeller(username, password, "Default", "Teller", "EMP001", "BR01");
                return new AuthContext(username, "BANK_TELLER", teller);
            }

            // Check customer login
            String sql = "SELECT * FROM customers WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String identifier = rs.getString("idNumber"); // Individual
                    if (identifier == null || identifier.isEmpty()) {
                        identifier = rs.getString("registrationNumber"); // Company
                    }
                    Customer customer = new BankingService().findCustomer(identifier);
                    if (customer != null) {
                        return new AuthContext(username, "CUSTOMER", customer);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // failed login
    }

    private boolean authenticateTeller(String username, String password) {
        String sql = "SELECT * FROM BankTeller WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bankingdb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // returns true if a row exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Setup credentials in database
    public void setupCustomerCredentials(Customer customer) {
        String username = generateConsistentUsername(customer);
        String password = generateSecurePassword();
        String identifier = customer instanceof Individual ?
                ((Individual) customer).getIdNumber() :
                ((Company) customer).getRegistrationNumber();

        customer.setUsername(username);
        customer.setPassword(password);

        // Save to database
        String sql = "UPDATE customers SET username = ?, password = ? WHERE idNumber = ? OR registrationNumber = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, identifier);
            stmt.setString(4, identifier);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // CHANGE password
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try (Connection conn = connect()) {
            // verify old password
            String sqlCheck = "SELECT * FROM customers WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
                stmt.setString(1, username);
                stmt.setString(2, oldPassword);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String identifier = rs.getString("idNumber");
                    if (identifier == null || identifier.isEmpty()) {
                        identifier = rs.getString("registrationNumber");
                    }

                    String sqlUpdate = "UPDATE customers SET password = ? WHERE idNumber = ? OR registrationNumber = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                        updateStmt.setString(1, newPassword);
                        updateStmt.setString(2, identifier);
                        updateStmt.setString(3, identifier);
                        updateStmt.executeUpdate();
                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // RESET password
    public void resetPassword(String identifier) {
        String newPassword = generateSecurePassword();
        String sql = "UPDATE customers SET password = ? WHERE idNumber = ? OR registrationNumber = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, identifier);
            stmt.setString(3, identifier);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Generate username consistently
    public String generateConsistentUsername(Customer customer) {
        String base = customer instanceof Individual ?
                ((Individual) customer).getFirstName().toLowerCase().replaceAll("\\s+", "") :
                ((Company) customer).getCompanyName().toLowerCase().replaceAll("\\s+", "").replaceAll("[^a-z0-9]", "");

        String id = customer instanceof Individual ?
                ((Individual) customer).getIdNumber() :
                ((Company) customer).getRegistrationNumber();

        int digits = Math.abs(id.hashCode() % 900) + 100;
        return base + digits;
    }

    // Generate secure password
    public String generateSecurePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String all = upper + lower + numbers;

        StringBuilder pw = new StringBuilder();
        pw.append(upper.charAt(random.nextInt(upper.length())));
        pw.append(lower.charAt(random.nextInt(lower.length())));
        pw.append(numbers.charAt(random.nextInt(numbers.length())));

        for (int i = 3; i < 8; i++) {
            pw.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle
        char[] arr = pw.toString().toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }

        return new String(arr);
    }
}
