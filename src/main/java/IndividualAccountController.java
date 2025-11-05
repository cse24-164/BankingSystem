package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class IndividualAccountController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField customerFirstNameField;
    @FXML
    private TextField customerSurnameField;
    @FXML
    private TextField customerIdField;
    @FXML
    private TextField customerDobField;
    @FXML
    private ComboBox<String> customerGenderComboBox;
    @FXML
    private TextField customerAddressField;
    @FXML
    private TextField customerPhoneField;
    @FXML
    private TextField customerEmailField;

    // Next of Kin fields
    @FXML
    private TextField nokNameField;
    @FXML
    private TextField nokRelationshipField;
    @FXML
    private ComboBox<String> nokGenderComboBox;
    @FXML
    private TextField nokPhoneField;

    // Income Information fields
    @FXML
    private ComboBox<String> incomeSourceTypeComboBox;
    @FXML
    private TextField monthlyIncomeField;
    @FXML
    private TextField employerNameField;
    @FXML
    private TextField employerAddressField;

    // Account fields
    @FXML
    private ComboBox<String> accountTypeComboBox;
    @FXML
    private TextField initialDepositField;
    @FXML
    private Label minimumDepositLabel;

    private BankTeller currentTeller;
    private TellerLandingController landingController;
    private BankingService bankingService;

    public void setBankTeller(BankTeller teller) {
        this.currentTeller = teller;
        this.bankingService = new BankingService();
        updateDashboard();
    }

    public void setLandingController(TellerLandingController landingController) {
        this.landingController = landingController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        customerGenderComboBox.getItems().addAll("Female", "Male");
        customerGenderComboBox.setValue("Female");

        nokGenderComboBox.getItems().addAll("Female", "Male");
        nokGenderComboBox.setValue("Female");

        incomeSourceTypeComboBox.getItems().addAll("EMPLOYMENT", "ALLOWANCE", "PENSION", "INVESTMENT", "OTHER");
        incomeSourceTypeComboBox.setValue("EMPLOYMENT");

        accountTypeComboBox.getItems().addAll("Savings", "Cheque", "Investment");
        accountTypeComboBox.setValue("Savings");

        accountTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateAccountRequirements(newValue));
        updateAccountRequirements("Savings");

        setupInputValidation();
    }

    private void updateDashboard() {
        if (currentTeller != null) {
            welcomeLabel.setText("Personal Customer Account Opening - " + currentTeller.getFullName() + " | " + currentTeller.getEmployeeId());
        }
    }

    private void updateAccountRequirements(String accountType) {
        switch (accountType) {
            case "Savings" -> minimumDepositLabel.setText("P50.00");
            case "Cheque" -> minimumDepositLabel.setText("P0.00");
            case "Investment" -> minimumDepositLabel.setText("P500.00");
        }
    }

    @FXML
    private void handleOpenAccount() {
        try {
            if (!validateIndividualInformation()) return;
            if (!validateNextOfKinInformation()) return;
            if (!validateIncomeInformation()) return;
            if (!validateDepositAmount()) return;

            double initialDeposit = Double.parseDouble(initialDepositField.getText());
            String accountType = accountTypeComboBox.getValue();

            if (!validateMinimumDeposit(accountType, initialDeposit)) return;

            Individual customer = new Individual(
                    customerFirstNameField.getText().trim(),
                    customerSurnameField.getText().trim(),
                    customerAddressField.getText().trim(),
                    customerIdField.getText().trim(),
                    parseDob(customerDobField.getText().trim()),
                    customerGenderComboBox.getValue(),
                    customerEmailField.getText().trim().isEmpty() ? "Not Provided" : customerEmailField.getText().trim(),
                    customerPhoneField.getText().trim(),
                    // Next of Kin
                    nokNameField.getText().trim(),
                    nokRelationshipField.getText().trim(),
                    nokGenderComboBox.getValue(),
                    nokPhoneField.getText().trim(),
                    // Income Source
                    incomeSourceTypeComboBox.getValue(),
                    employerNameField.getText().trim(),
                    employerAddressField.getText().trim(),
                    Double.parseDouble(monthlyIncomeField.getText().trim())
            );

            LoginService loginService = new LoginService();
            String username = loginService.generateConsistentUsername(customer);
            String password = loginService.generateSecurePassword();
            customer.setUsername(username);
            customer.setPassword(password);

            // Save customer
            try {
                bankingService.createCustomer(customer);
            } catch (Exception e) {
                showAlert("Customer Creation Error", "Failed to create customer: " + e.getMessage());
                return;
            }

            // Open account
            Account newAccount = currentTeller.openAccount(customer, accountType.toLowerCase(), currentTeller.getBranchCode(), initialDeposit);

            if (newAccount != null) {
                showSuccessAlert(customer, newAccount, initialDeposit);

                if (landingController != null) {
                    landingController.addAccountOpened(customer.getFullName(), accountType);
                    landingController.addCustomerCreated(customer.getFullName());
                }

                clearForm();
            } else {
                showAlert("Account Opening Failed", "Failed to open individual account. Please check requirements.");
            }

        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values for deposit and income amounts.");
        } catch (Exception e) {
            showAlert("Error", "Error opening account: " + e.getMessage());
        }
    }

    private void showSuccessAlert(Individual customer, Account newAccount, double initialDeposit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Opened Successfully");
        alert.setHeaderText("Successfully Opened Personal Customer Account\n");

        String content = "Customer: " + customer.getFullName() + "\n" +
                "ID Number: " + customer.getIdNumber() + "\n" +
                "Account Type: " + accountTypeComboBox.getValue() + " Account\n" +
                "Account Number: #" + newAccount.getAccountNumber() + "\n" +
                "Initial Deposit: P" + String.format("%.2f", initialDeposit) + "\n" +
                "Current Balance: P" + String.format("%.2f", newAccount.getBalance()) + "\n" +
                "Next of Kin: " + customer.getNextOfKinName() + " (" + customer.getNextOfKinRelationship() + ")\n" +
                "Income Source: " + customer.getSourceOfIncome() + " - " + customer.getEmployerName() + "\n" +
                "Monthly Income: P" + String.format("%.2f", customer.getMonthlyIncome()) + "\n\n" +
                "** CUSTOMER LOGIN CREDENTIALS **\n" +
                "   Username: " + customer.getUsername() + "\n" +
                "   Password: " + customer.getPassword() + "\n\n" +
                "\n Opened by: " + currentTeller.getFullName() + "\n" +
                "Branch: " + currentTeller.getBranchCode() + "\n" +
                "Date: " + new Date() + "\n\n";

        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean validateIndividualInformation() {
        if (customerFirstNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter first name");
            return false;
        }
        if (customerSurnameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter surname");
            return false;
        }
        if (customerIdField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter ID number");
            return false;
        }
        if (customerDobField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter DOB");
            return false;
        }
        if (customerAddressField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter address");
            return false;
        }
        if (customerPhoneField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter phone number");
            return false;
        }
        return true;
    }

    private boolean validateNextOfKinInformation() {
        if (nokNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter next of kin name");
            return false;
        }
        if (nokRelationshipField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter next of kin relationship");
            return false;
        }
        if (nokPhoneField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter next of kin phone");
            return false;
        }
        return true;
    }

    private boolean validateIncomeInformation() {
        if (monthlyIncomeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter monthly income");
            return false;
        }
        if (employerNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter employer/provider name");
            return false;
        }
        try {
            double income = Double.parseDouble(monthlyIncomeField.getText().trim());
            if (income <= 0) {
                showAlert("Validation Error", "Income must be > 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Invalid income value");
            return false;
        }
        return true;
    }

    private boolean validateDepositAmount() {
        if (initialDepositField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Enter initial deposit");
            return false;
        }
        try {
            double deposit = Double.parseDouble(initialDepositField.getText().trim());
            if (deposit <= 0) {
                showAlert("Validation Error", "Deposit must be > 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Invalid deposit value");
            return false;
        }
        return true;
    }

    private boolean validateMinimumDeposit(String accountType, double deposit) {
        double minimum = switch (accountType) {
            case "Savings" -> 50.0;
            case "Cheque" -> 0;
            case "Investment" -> 500.0;
            default -> 0.0;
        };

        if (deposit < minimum) {
            showAlert("Validation Error",
                    String.format("Minimum deposit for %s account is P%.2f", accountType, minimum));
            initialDepositField.requestFocus();
            return false;
        }
        return true;
    }

}
