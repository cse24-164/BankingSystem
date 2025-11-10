package com.example.bankaccount;

import java.sql.*;
import java.util.*;

public class JDBCCustomerDAO implements CustomerDAO {

    @Override
    public void saveCustomer(Customer customer) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String customerSql = """
                INSERT INTO customers (customer_id, customer_type, username, password, address, email, phone)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            try (PreparedStatement stmt = conn.prepareStatement(customerSql)) {
                stmt.setString(1, customer.getIdentificationNumber());
                stmt.setString(2, customer instanceof Individual ? "INDIVIDUAL" : "COMPANY");
                stmt.setString(3, customer.getUsername());
                stmt.setString(4, customer.getPassword());
                stmt.setString(5, customer.getAddress());
                stmt.setString(6, customer.getEmail());
                stmt.setString(7, customer.getPhoneNumber());
                stmt.executeUpdate();
            }

            if (customer instanceof Individual) {
                saveIndividual(conn, (Individual) customer);
            } else if (customer instanceof Company) {
                saveCompany(conn, (Company) customer);
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Error saving customer", e);
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    private void saveIndividual(Connection conn, Individual individual) throws SQLException {
        String sql = """
            INSERT INTO individual_customers (
                customer_id, first_name, surname, id_number, date_of_birth, gender,
                nok_name, nok_relationship, nok_gender, nok_phone, nok_address,
                income_type, income_source_name, employer_address, monthly_income
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, individual.getIdentificationNumber());
            stmt.setString(2, individual.getFirstName());
            stmt.setString(3, individual.getSurname());
            stmt.setString(4, individual.getIdNumber());
            stmt.setDate(5, new java.sql.Date(individual.getDateOfBirth().getTime()));
            stmt.setString(6, individual.getGender());

            // Next of Kin
            stmt.setString(7, individual.getNextOfKinName());
            stmt.setString(9, individual.getNextOfKinRelationship());
            stmt.setString(10, individual.getNextOfKinGender());
            stmt.setString(11, individual.getNextOfKinPhoneNumber());

            // Income
            stmt.setString(13, individual.getSourceOfIncome());
            stmt.setString(14, individual.getSourceName());
            stmt.setString(15, individual.getEmployerAddress());
            stmt.setDouble(16, individual.getMonthlyIncome());

            stmt.executeUpdate();
        }
    }

    private void saveCompany(Connection conn, Company company) throws SQLException {
        String sql = """
        INSERT INTO company_customers (
            customer_id, company_name, registration_number, business_type,
            contact_person, source_of_income, annual_revenue
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, company.getIdentificationNumber());
            stmt.setString(2, company.getCompanyName());
            stmt.setString(3, company.getRegistrationNumber());
            stmt.setString(4, company.getBusinessType());
            stmt.setString(5, company.getContactPersonName());
            stmt.setString(6, company.getSourceOfIncome());
            stmt.setDouble(7, company.getAnnualRevenue());
            stmt.executeUpdate();
        }
    }


    @Override
    public Customer findCustomerById(String id) {
        String sql = """
            SELECT c.*, ic.*, cc.*
            FROM customers c
            LEFT JOIN individual_customers ic ON c.customer_id = ic.customer_id
            LEFT JOIN company_customers cc ON c.customer_id = cc.customer_id
            WHERE c.customer_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapCustomer(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer", e);
        }
    }

    @Override
    public List<Customer> findAllCustomers() {
        String sql = """
            SELECT c.*, ic.*, cc.*
            FROM customers c
            LEFT JOIN individual_customers ic ON c.customer_id = ic.customer_id
            LEFT JOIN company_customers cc ON c.customer_id = cc.customer_id
            ORDER BY c.registration_date DESC
            """;

        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
            return customers;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all customers", e);
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Update base customer
            String customerSql = """
                UPDATE customers SET username = ?, password = ?, address = ?, email = ?, phone = ? 
                WHERE customer_id = ?
                """;
            try (PreparedStatement stmt = conn.prepareStatement(customerSql)) {
                stmt.setString(1, customer.getUsername());
                stmt.setString(2, customer.getPassword());
                stmt.setString(3, customer.getAddress());
                stmt.setString(4, customer.getEmail());
                stmt.setString(5, customer.getPhoneNumber());
                stmt.executeUpdate();
            }

            // Update specific table
            if (customer instanceof Individual) {
                updateIndividual(conn, (Individual) customer);
            } else if (customer instanceof Company) {
                updateCompany(conn, (Company) customer);
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Error updating customer", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    private void updateIndividual(Connection conn, Individual individual) throws SQLException {
        String sql = """
        UPDATE individual_customers SET
            first_name = ?, 
            surname = ?, 
            address = ?, 
            id_number = ?, 
            date_of_birth = ?, 
            gender = ?, 
            email = ?, 
            phone = ?,
            -- Next of Kin
            nok_name = ?, 
            nok_relationship = ?, 
            nok_gender = ?, 
            nok_phone = ?,
            -- Source of Income
            income_type = ?, 
            income_source_name = ?, 
            employer_address = ?, 
            monthly_income = ?
        WHERE customer_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, individual.getFirstName());
            stmt.setString(2, individual.getSurname());
            stmt.setString(3, individual.getAddress());
            stmt.setString(4, individual.getIdNumber());
            stmt.setDate(5, new java.sql.Date(individual.getDateOfBirth().getTime()));
            stmt.setString(6, individual.getGender());
            stmt.setString(7, individual.getEmail());
            stmt.setString(8, individual.getPhoneNumber());

            // Next of Kin
            stmt.setString(9, individual.getNextOfKinName());
            stmt.setString(10, individual.getNextOfKinRelationship());
            stmt.setString(11, individual.getNextOfKinGender());
            stmt.setString(12, individual.getNextOfKinPhoneNumber());

            // Source of Income
            stmt.setString(13, individual.getSourceOfIncome());
            stmt.setString(14, individual.getSourceName());
            stmt.setString(15, individual.getEmployerAddress());
            stmt.setDouble(16, individual.getMonthlyIncome());

            stmt.setString(17, individual.getIdentificationNumber());
            stmt.executeUpdate();
        }
    }


    private void updateCompany(Connection conn, Company company) throws SQLException {
        String sql = """
        UPDATE company_customers SET 
            company_name = ?, 
            registration_number = ?, 
            business_type = ?, 
            contact_person = ?, 
            source_of_income = ?, 
            annual_revenue = ?, 
            address = ?, 
            email = ?, 
            phone = ?
        WHERE customer_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, company.getCompanyName());
            stmt.setString(2, company.getRegistrationNumber());
            stmt.setString(3, company.getBusinessType());
            stmt.setString(4, company.getContactPersonName());
            stmt.setString(5, company.getSourceOfIncome());
            stmt.setDouble(6, company.getAnnualRevenue());
            stmt.setString(7, company.getAddress());
            stmt.setString(8, company.getEmail());
            stmt.setString(9, company.getPhoneNumber());
            stmt.setString(10, company.getIdentificationNumber());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteCustomer(String id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting customer", e);
        }
    }

    @Override
    public boolean customerExists(String id) {
        String sql = "SELECT 1 FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Error checking customer exists", e);
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        String customerType = rs.getString("customer_type");

        if ("INDIVIDUAL".equals(customerType)) {
            return mapIndividual(rs);
        } else if ("COMPANY".equals(customerType)) {
            return mapCompany(rs);
        }
        return null;
    }

    private Individual mapIndividual(ResultSet rs) throws SQLException {
        Individual individual = new Individual(
                rs.getString("first_name"),
                rs.getString("surname"),
                rs.getString("address"),
                rs.getString("id_number"),
                rs.getDate("date_of_birth"),
                rs.getString("gender"),
                rs.getString("email"),
                rs.getString("phone"),
                // Next-of-Kin
                rs.getString("nok_name"),
                rs.getString("nok_relationship"),
                rs.getString("nok_gender"),
                rs.getString("nok_phone"),
                // Source of Income
                rs.getString("income_type"),
                rs.getString("income_source_name"),
                rs.getString("employer_address"),
                rs.getDouble("monthly_income")
        );

        individual.setUsername(rs.getString("username"));
        individual.setPassword(rs.getString("password"));
        return individual;
    }

    private Company mapCompany(ResultSet rs) throws SQLException {
        Company company = new Company(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("address"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("company_name"),
                rs.getString("registration_number"),
                rs.getString("business_type"),
                rs.getString("contact_person"),
                rs.getString("source_of_income"),
                rs.getDouble("annual_revenue")
        );
        return company;
    }
}
