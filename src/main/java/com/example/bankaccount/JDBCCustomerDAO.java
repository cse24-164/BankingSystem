package com.example.bankaccount;

import com.example.bankaccount.Customer;
import com.example.bankaccount.DatabaseConnection;
import com.example.bankaccount.LoginService;
import com.example.bankaccount.Individual;
import com.example.bankaccount.Company;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JDBCCustomerDAO implements CustomerDAO {

    @Override
    public void saveCustomer(Customer customer) {
        String insertUserSql = "INSERT INTO user (username, password, userType) VALUES (?, ?, ?)";
        String insertCustomerSql = "INSERT INTO customer (userId, address, email, phoneNumber, customerType) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1) Set username for customer
            if (customer.getUsername() == null || customer.getPassword() == null) {
                LoginService ls = new LoginService();
                customer.setUsername(ls.generateConsistentUsername(customer));
                customer.setPassword(ls.generateSecurePassword());
            }

            // 2) Insert into user table and get generated userId
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, customer.getUsername());
                userStmt.setString(2, customer.getPassword());
                userStmt.setString(3, "CUSTOMER");
                int affected = userStmt.executeUpdate();
                if (affected == 0) throw new SQLException("Creating user failed, no rows affected.");
                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        customer.setUserId(keys.getInt(1));
                    } else {
                        throw new SQLException("Creating user failed, no id obtained.");
                    }
                }
            }

            // 3) Insert into customer table using generated userId
            try (PreparedStatement custStmt = conn.prepareStatement(insertCustomerSql, Statement.RETURN_GENERATED_KEYS)) {
                custStmt.setInt(1, customer.getUserId());
                custStmt.setString(2, customer.getAddress());
                custStmt.setString(3, customer.getEmail());
                custStmt.setString(4, customer.getPhoneNumber());
                custStmt.setString(5, customer.getCustomerType()); // "INDIVIDUAL" or "COMPANY"
                int affected = custStmt.executeUpdate();
                if (affected == 0) throw new SQLException("Creating customer failed, no rows affected.");
                try (ResultSet keys = custStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        customer.setCustomerId(keys.getInt(1));
                    } else {
                        throw new SQLException("Creating customer failed, no id obtained.");
                    }
                }
            }

            // 4) Insert into child table
            if (customer instanceof Individual individual) {
                insertIndividual(conn, individual);
            } else if (customer instanceof Company company) {
                insertCompany(conn, company);
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving customer", e);
        }
    }

    private void insertIndividual(Connection conn, Individual individual) throws SQLException {
        String sql = """
            INSERT INTO individual (
                customerId, firstName, surname, idNumber, dateOfBirth, gender,
                nextOfKinName, nextOfKinRelationship, nextOfKinGender, nextOfKinPhoneNumber,
                sourceOfIncome, sourceName, sourceAddress, monthlyIncome
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, individual.getCustomerId());
            stmt.setString(2, individual.getFirstName());
            stmt.setString(3, individual.getSurname());
            stmt.setString(4, individual.getIdNumber());
            if (individual.getDateOfBirth() != null) {
                stmt.setDate(5, new java.sql.Date(individual.getDateOfBirth().getTime()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setString(6, individual.getGender());

            stmt.setString(7, individual.getNextOfKinName());
            stmt.setString(8, individual.getNextOfKinRelationship());
            stmt.setString(9, individual.getNextOfKinGender());
            stmt.setString(10, individual.getNextOfKinPhoneNumber());

            stmt.setString(11, individual.getSourceOfIncome());
            stmt.setString(12, individual.getSourceName());
            stmt.setString(13, individual.getEmployerAddress());
            stmt.setDouble(14, individual.getMonthlyIncome());

            stmt.executeUpdate();
        }
    }

    private void insertCompany(Connection conn, Company company) throws SQLException {
        String sql = """
            INSERT INTO company (
                customerId, companyName, registrationNumber, businessType,
                contactPersonName, sourceOfIncome, annualRevenue
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, company.getCustomerId());
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
    public Customer findCustomerById(int customerId) {
        String baseSql = "SELECT c.customerId, c.userId, c.address, c.email, c.phoneNumber, c.customerType, u.username, u.password " +
                "FROM customer c JOIN user u ON c.userId = u.userId WHERE c.customerId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(baseSql)) {

            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                int userId = rs.getInt("userId");
                String address = rs.getString("address");
                String email = rs.getString("email");
                String phone = rs.getString("phoneNumber");
                String customerType = rs.getString("customerType");
                String username = rs.getString("username");
                String password = rs.getString("password");

                if ("INDIVIDUAL".equalsIgnoreCase(customerType)) {
                    // load individual row
                    String indSql = "SELECT * FROM individual WHERE customerId = ?";
                    try (PreparedStatement indStmt = conn.prepareStatement(indSql)) {
                        indStmt.setInt(1, customerId);
                        try (ResultSet indRs = indStmt.executeQuery()) {
                            if (!indRs.next()) throw new SQLException("Individual row missing for customerId " + customerId);
                            String idNumber = indRs.getString("idNumber");
                            String firstName = indRs.getString("firstName");
                            String surname = indRs.getString("surname");

                            java.sql.Date sqlDob = indRs.getDate("dateOfBirth");
                            java.util.Date dob = (sqlDob != null) ? new java.util.Date(sqlDob.getTime()) : null;
                            String gender = indRs.getString("gender");

                            String nokName = indRs.getString("nextOfKinName");
                            String nokRel = indRs.getString("nextOfKinRelationship");
                            String nokGender = indRs.getString("nextOfKinGender");
                            String nokPhone = indRs.getString("nextOfKinPhoneNumber");

                            String sourceOfIncome = indRs.getString("sourceOfIncome");
                            String sourceName = indRs.getString("sourceName");
                            String sourceAddress = indRs.getString("sourceAddress");
                            double monthlyIncome = indRs.getDouble("monthlyIncome");


                            Individual ind = new Individual(
                                    username,
                                    password,
                                    address,
                                    email,
                                    firstName,
                                    surname,
                                    idNumber,
                                    dob,
                                    gender,
                                    phone,
                                    nokName,
                                    nokRel,
                                    nokGender,
                                    nokPhone,
                                    sourceOfIncome,
                                    sourceName,
                                    sourceAddress,
                                    monthlyIncome
                            );

                            ind.setCustomerId(customerId);
                            ind.setUserId(userId);
                            return ind;
                        }
                    }
                } else if ("COMPANY".equalsIgnoreCase(customerType)) {
                    String compSql = "SELECT * FROM company WHERE customerId = ?";
                    try (PreparedStatement compStmt = conn.prepareStatement(compSql)) {
                        compStmt.setInt(1, customerId);
                        try (ResultSet compRs = compStmt.executeQuery()) {
                            if (!compRs.next()) throw new SQLException("Company row missing for customerId " + customerId);

                            String companyName = compRs.getString("companyName");
                            String registrationNumber = compRs.getString("registrationNumber");
                            String businessType = compRs.getString("businessType");
                            String contactPerson = compRs.getString("contactPersonName");
                            String sourceOfIncome = compRs.getString("sourceOfIncome");
                            double annualRevenue = compRs.getDouble("annualRevenue");

                            Company company = new Company(
                                    username,
                                    password,
                                    address,
                                    email,
                                    phone,
                                    "COMPANY",
                                    companyName,
                                    registrationNumber,
                                    businessType,
                                    contactPerson,
                                    sourceOfIncome,
                                    annualRevenue
                            );

                            company.setCustomerId(customerId);
                            company.setUserId(userId);
                            return company;
                        }
                    }
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by id", e);
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT userId FROM user WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if user exists

        } catch (SQLException e) {
            throw new RuntimeException("Error checking username: " + username, e);
        }
    }

    public void updateUserPassword(String username, String newPassword) {
        String sql = "UPDATE user SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating password for username: " + username, e);
        }
    }


    @Override
    public List<Customer> findAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.customerId FROM customer c ORDER BY c.customerId DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int cid = rs.getInt("customerId");
                Customer c = findCustomerById(cid); // reuse single-ID loader
                if (c != null) list.add(c);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all customers", e);
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        String updateBase = "UPDATE customer SET address = ?, email = ?, phoneNumber = ? WHERE customerId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateBase)) {

            stmt.setString(1, customer.getAddress());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setInt(4, customer.getCustomerId());
            stmt.executeUpdate();

            if (customer instanceof Individual ind) {
                String updateInd = """
                    UPDATE individual SET firstName=?, surname=?, idNumber=?, dateOfBirth=?, gender=?,
                        nextOfKinName=?, nextOfKinRelationship=?, nextOfKinGender=?, nextOfKinPhoneNumber=?,
                        sourceOfIncome=?, sourceName=?, sourceAddress=?, monthlyIncome=?
                    WHERE customerId = ?
                    """;
                try (PreparedStatement indStmt = conn.prepareStatement(updateInd)) {
                    indStmt.setString(1, ind.getFirstName());
                    indStmt.setString(2, ind.getSurname());
                    indStmt.setString(3, ind.getIdNumber());
                    if (ind.getDateOfBirth() != null) indStmt.setDate(4, new java.sql.Date(ind.getDateOfBirth().getTime()));
                    else indStmt.setNull(4, Types.DATE);
                    indStmt.setString(5, ind.getGender());
                    indStmt.setString(6, ind.getNextOfKinName());
                    indStmt.setString(7, ind.getNextOfKinRelationship());
                    indStmt.setString(8, ind.getNextOfKinGender());
                    indStmt.setString(9, ind.getNextOfKinPhoneNumber());
                    indStmt.setString(10, ind.getSourceOfIncome());
                    indStmt.setString(11, ind.getSourceName());
                    indStmt.setString(12, ind.getEmployerAddress());
                    indStmt.setDouble(13, ind.getMonthlyIncome());
                    indStmt.setInt(14, ind.getCustomerId());
                    indStmt.executeUpdate();
                }
            } else if (customer instanceof Company comp) {
                String updateComp = """
                    UPDATE company SET companyName=?, registrationNumber=?, businessType=?, contactPersonName=?,
                        sourceOfIncome=?, annualRevenue=?
                    WHERE customerId = ?
                    """;
                try (PreparedStatement compStmt = conn.prepareStatement(updateComp)) {
                    compStmt.setString(1, comp.getCompanyName());
                    compStmt.setString(2, comp.getRegistrationNumber());
                    compStmt.setString(3, comp.getBusinessType());
                    compStmt.setString(4, comp.getContactPersonName());
                    compStmt.setString(5, comp.getSourceOfIncome());
                    compStmt.setDouble(6, comp.getAnnualRevenue());
                    compStmt.setInt(7, comp.getCustomerId());
                    compStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error updating customer", e);
        }
    }

    @Override
    public void deleteCustomer(int id) {
        String sql = "DELETE FROM customer WHERE customerId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting customer", e);
        }
    }

    @Override
    public List<Individual> getAllIndividuals() {
        List<Individual> individuals = new ArrayList<>();

        String sql = "SELECT c.customerId, c.address, c.email, c.phoneNumber, c.registrationDate, c.CustomerType, " +
                "i.idNumber, i.firstName, i.surname, i.dateOfBirth, i.gender," +
                "i.nextOfKinName, i.nextOfKinRelationship, i.nextOfKinGender, i.nextOfKinPhoneNumber, " +
                "i.sourceOfIncome, i.sourceName, i.sourceAddress, i.monthlyIncome " +
                "FROM customer c " +
                "JOIN individual i ON c.customerId = i.customerId " +
                "WHERE c.customerType = 'INDIVIDUAL'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String idNumber = rs.getString("idNumber");
                int customerId = rs.getInt("customerId");
                String firstName = rs.getString("firstName");
                String surname = rs.getString("surname");
                java.sql.Date sqlDob = rs.getDate("dateOfBirth");
                java.util.Date dateOfBirth =
                        sqlDob != null ? new java.util.Date(sqlDob.getTime()) : null;

                String gender = rs.getString("gender");

                String nextOfKinName = rs.getString("nextOfKinName");
                String nextOfKinRelationship = rs.getString("nextOfKinRelationship");
                String nextOfKinGender = rs.getString("nextOfKinGender");
                String nextOfKinPhoneNumber = rs.getString("nextOfKinPhoneNumber");

                String sourceOfIncome = rs.getString("sourceOfIncome");
                String sourceName = rs.getString("sourceName");
                String sourceAddress = rs.getString("sourceAddress");
                double monthlyIncome = rs.getDouble("monthlyIncome");

                Individual individual = new Individual(
                        firstName, surname, idNumber, dateOfBirth, gender,
                        nextOfKinName, nextOfKinRelationship, nextOfKinGender, nextOfKinPhoneNumber,
                        sourceOfIncome, sourceName, sourceAddress, monthlyIncome
                );


                individual.setCustomerId(customerId);
                individual.setAddress(rs.getString("address"));
                individual.setEmail(rs.getString("email"));
                individual.setPhoneNumber(rs.getString("phoneNumber"));

                individuals.add(individual);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading individuals", e);
        }

        return individuals;
    }

    @Override
    public List<Company> getAllCompanies() {
        List<Company> companies = new ArrayList<>();

        String sql = """
        SELECT c.customerId, c.address, c.email, c.phoneNumber, c.customerType,
               cc.companyName, cc.registrationNumber, cc.businessType,
               cc.contactPersonName, cc.sourceOfIncome, cc.annualRevenue
        FROM customer c
        JOIN company cc ON c.customerId = cc.customerId
        WHERE c.customerType = 'COMPANY'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Company company = new Company(
                        null, // username
                        null, // password
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("phoneNumber"),
                        "COMPANY",
                        rs.getString("companyName"),
                        rs.getString("registrationNumber"),
                        rs.getString("businessType"),
                        rs.getString("contactPersonName"),
                        rs.getString("sourceOfIncome"),
                        rs.getDouble("annualRevenue")
                );

                company.setCustomerId(rs.getInt("customerId"));
                company.setAddress(rs.getString("address"));
                company.setEmail(rs.getString("email"));
                company.setPhoneNumber(rs.getString("phoneNumber"));

                companies.add(company);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading companies", e);
        }

        return companies;
    }


    @Override
    public boolean customerExists(int id) {
        String sql = "SELECT 1 FROM customer WHERE customerId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking customer exists", e);
        }
    }

}
