package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class CompanyAccountController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TextField companyNameField;
    @FXML private TextField registrationNumberField;
    @FXML private TextField businessTypeField;
    @FXML private TextField annualRevenueField;
    @FXML private TextField contactPersonField;
    @FXML private TextField customerAddressField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerEmailField;
    @FXML private ComboBox<String> accountTypeComboBox;
    @FXML private TextField initialDepositField;
    @FXML private Label minimumDepositLabel;

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
        accountTypeComboBox.getItems().addAll("Savings", "Investment");
        accountTypeComboBox.setValue("Savings");

        accountTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateAccountRequirements(newValue));

        updateAccountRequirements("Savings");
        setupInputValidation();
    }

    private void updateDashboard() {
        if (currentTeller != null) {
            welcomeLabel.setText("Company Account Opening - " + currentTeller.getFullName());
        }
    }

    private void updateAccountRequirements(String accountType) {
        switch (accountType) {
            case "Savings": minimumDepositLabel.setText("P50.00"); break;
            case "Investment": minimumDepositLabel.setText("P500.00"); break;
            default: minimumDepositLabel.setText("P0.00");
        }
    }

    @FXML
    private void handleOpenAccount() {
        try {
            if (!validateForm()) return;

            double initialDeposit = Double.parseDouble(initialDepositField.getText());
            String accountType = accountTypeComboBox.getValue();

            if (!validateMinimumDeposit(accountType, initialDeposit)) return;

            Company customer = createCompanyFromForm();
            setupCustomerCredentials(customer);

            if (!saveCustomer(customer)) return;

            Account newAccount = currentTeller.openAccount(
                    customer,
                    accountType.toLowerCase(),
                    currentTeller.getBranchCode(),
                    initialDeposit
            );

            if (newAccount != null) {
                handleSuccess(customer, newAccount, accountType, initialDeposit);
                clearForm();
            } else {
                showAlert("Account Opening Failed", "Failed to open company account.");
            }

        } catch (Exception e) {
            showAlert("Error", "Error opening account: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (companyNameField.getText().trim().isEmpty()) { showAlert("Validation Error", "Please enter company name"); companyNameField.requestFocus(); return false; }
        if (registrationNumberField.getText().trim().isEmpty()) { showAlert("Validation Error", "Please enter registration number"); registrationNumberField.requestFocus(); return false; }
        if (businessTypeField.getText().trim().isEmpty()) { showAlert("Validation Error", "Please enter business type"); businessTypeField.requestFocus(); return false; }
        if (contactPersonField.getText().trim().isEmpty()) { showAlert("Validation Error", "Please enter contact person name"); contactPersonField.requestFocus(); return false; }
        if (customerAddressField.getText().trim().isEmpty()) { showAlert("Validation Error", "Please enter company address"); customerAddressField.requestFocus(); return false; }
        if (customerPhoneField.getText().trim().isEmpty()) { showAlert("Validation Error", "Please enter company phone number"); customerPhoneField.requestFocus(); return false; }

        try {
            double revenue = Double.parseDouble(annualRevenueField.getText().trim());
            if (revenue < 0) { showAlert("Validation Error", "Annual revenue must be positive"); annualRevenueField.requestFocus(); return false; }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid annual revenue"); annualRevenueField.requestFocus(); return false;
        }

        if (initialDepositField.getText().trim().isEmpty()) { showAlert("Validation Error", "Please enter initial deposit"); initialDepositField.requestFocus(); return false; }

        try {
            double deposit = Double.parseDouble(initialDepositField.getText().trim());
            if (deposit < 0) { showAlert("Validation Error", "Initial deposit cannot be negative"); initialDepositField.requestFocus(); return false; }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid initial deposit"); initialDepositField.requestFocus(); return false;
        }

        return true;
    }

    private boolean validateMinimumDeposit(String accountType, double deposit) {
        double minimumDeposit = getMinimumDeposit(accountType);
        if (deposit < minimumDeposit) {
            showAlert("Validation Error", String.format("Minimum deposit for %s account is P%.2f", accountType, minimumDeposit));
            initialDepositField.requestFocus();
            return false;
        }
        return true;
    }

    private double getMinimumDeposit(String accountType) {
        switch (accountType) {
            case "Savings": return 50.00;
            case "Investment": return 500.00;
            default: return 0.00;
        }
    }

    private Company createCompanyFromForm() {
        return new Company(
                companyNameField.getText().trim(),
                registrationNumberField.getText().trim(),
                businessTypeField.getText().trim(),
                contactPersonField.getText().trim(),
                "", // sourceOfIncome removed
                Double.parseDouble(annualRevenueField.getText().trim()),
                customerAddressField.getText().trim(),
                customerEmailField.getText().trim().isEmpty() ? "Not Provided" : customerEmailField.getText().trim(),
                customerPhoneField.getText().trim()
        );
    }

    private void setupCustomerCredentials(Company customer) {
        LoginService loginService = new LoginService();
        customer.setUsername(loginService.generateConsistentUsername(customer));
        customer.setPassword(loginService.generateSecurePassword());
    }

    private boolean saveCustomer(Company customer) {
        try {
            bankingService.createCustomer(customer);
            return true;
        } catch (Exception e) {
            showAlert("Customer Creation Error", "Failed to create company customer: " + e.getMessage());
            return false;
        }
    }

    private void handleSuccess(Company customer, Account newAccount, String accountType, double initialDeposit) {
        showSuccessAlert(customer, newAccount, accountType, initialDeposit);

        if (landingController != null) {
            landingController.addAccountOpened(customer.getDisplayName(), accountType);
            landingController.addCustomerCreated(customer.getDisplayName());
        }
    }

    private void showSuccessAlert(Company customer, Account newAccount, String accountType, double initialDeposit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Opened Successfully");
        alert.setHeaderText("Company Account Opened Successfully");

        String content = "Company: " + customer.getDisplayName() + "\n" +
                "Registration: " + customer.getIdentificationNumber() + "\n" +
                "Business Type: " + customer.getBusinessType() + "\n" +
                "Contact Person: " + customer.getContactPersonName() + "\n" +
                "Annual Revenue: P" + String.format("%.2f", customer.getAnnualRevenue()) + "\n" +
                "Account Type: " + accountType + " Account\n" +
                "Account Number: #" + newAccount.getAccountNumber() + "\n" +
                "Initial Deposit: P" + String.format("%.2f", initialDeposit) + "\n" +
                "Current Balance: P" + String.format("%.2f", newAccount.getBalance()) + "\n\n" +
                "Login Credentials:\n" +
                "Username: " + customer.getUsername() + "\n" +
                "Password: " + customer.getPassword() + "\n\n" +
                "Opened by: " + currentTeller.getFullName() + "\n" +
                "Branch: " + currentTeller.getBranchCode() + "\n" +
                "Date: " + new Date();

        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        showAlert("Form Cleared", "Form cleared successfully.");
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
    }

    private void clearForm() {
        companyNameField.clear();
        registrationNumberField.clear();
        businessTypeField.clear();
        annualRevenueField.clear();
        contactPersonField.clear();
        customerAddressField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        initialDepositField.clear();
        accountTypeComboBox.setValue("Savings");
        companyNameField.requestFocus();
    }

    private void setupInputValidation() {
        setupNumericField(annualRevenueField);
        setupNumericField(initialDepositField);
        setupPhoneField(customerPhoneField);
    }

    private void setupNumericField(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) field.setText(oldValue);
        });
    }

    private void setupPhoneField(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) field.setText(oldValue);
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
