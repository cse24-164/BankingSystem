package com.example.bankaccount;

import java.sql.*;
import java.util.Random;

public class LoginService {

    public AuthContext login(String username, String password) {
        // single connection entrypoint
        String sql = """
            SELECT u.userId, u.username, u.password, u.userType, c.customerId, c.customerType
            FROM user u
            JOIN customer c ON u.userId = c.userId
            WHERE u.username = ? AND u.password = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Teller short-circuit: if you keep tellers in a separate table, check them first
            if (authenticateTeller(username, password)) {
                BankTeller teller = new BankTeller(username, password, "Default", "Teller", "EMP001", "BR01");
                return new AuthContext(username, "BANK_TELLER", teller);
            }

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int customerId = rs.getInt("customerId");

                    BankingService bankingService = new BankingService();
                    Customer customer = bankingService.findCustomerById(customerId);
                    if (customer != null) {
                        return new AuthContext(username, "CUSTOMER", customer);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Login failed", e);
        }

        return null;
    }

    private boolean authenticateTeller(String username, String password) {
        String sql = "SELECT 1 FROM BankTeller WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String generateConsistentUsername(Customer customer) {
        String base = customer instanceof Individual ?
                ((Individual) customer).getFirstName().toLowerCase().replaceAll("\\s+", "") :
                ((Company) customer).getCompanyName().toLowerCase().replaceAll("\\s+", "").replaceAll("[^a-z0-9]", "");

        String id = customer instanceof Individual ?
                ((Individual) customer).getIdNumber() :
                ((Company) customer).getRegistrationNumber();

        int digits = Math.abs(id.hashCode() % 900) + 100;  // produces 100–999
        return base + digits;  // e.g. "maipelo472"
    }

    public String generateSecurePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String all = upper + lower + numbers;

        StringBuilder pw = new StringBuilder();
        Random random = new Random();

        // Guaranteed characters
        pw.append(upper.charAt(random.nextInt(upper.length())));
        pw.append(lower.charAt(random.nextInt(lower.length())));
        pw.append(numbers.charAt(random.nextInt(numbers.length())));

        // Remaining characters
        for (int i = 3; i < 8; i++) {
            pw.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle characters
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
