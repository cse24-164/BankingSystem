package com.example.bankaccount;
import com.example.bankaccount.*;


import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.net.URL;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.ResourceBundle;

public class IndividualAccountController implements Initializable {

    @FXML
    private Label welcomeLabel;

    // Customer Info
    @FXML private TextField customerFirstNameField;
    @FXML private TextField customerSurnameField;
    @FXML private TextField customerIdField;
    @FXML private DatePicker customerDobField;

    @FXML private ComboBox<String> customerGenderComboBox;
    @FXML private TextField customerAddressField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerEmailField;

    // Next of Kin
    @FXML private TextField nokNameField;
    @FXML private ComboBox<String> nokRelationshipComboBox;
    @FXML private ComboBox<String> nokGenderComboBox;
    @FXML private TextField nokPhoneField;

    // Income Info
    @FXML private ComboBox<String> incomeSourceTypeComboBox;
    @FXML private TextField monthlyIncomeField;
    @FXML private TextField employerNameField;
    @FXML private TextField employerAddressField;

    // Account
    @FXML private ComboBox<String> accountTypeComboBox;
    @FXML private TextField initialDepositField;
    @FXML private Label minimumDepositLabel;

    private com.example.bankaccount.BankTeller currentTeller;
    private com.example.bankaccount.TellerLandingController landingController;
    private com.example.bankaccount.BankingService bankingService;

    public void setBankTeller(com.example.bankaccount.BankTeller teller) {
        this.currentTeller = teller;
        this.bankingService = new com.example.bankaccount.BankingService();
        updateDashboard();
    }

    public void setLandingController(com.example.bankaccount.TellerLandingController landingController) {
        this.landingController = landingController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        customerGenderComboBox.getItems().addAll("Female", "Male");
        customerGenderComboBox.setValue("Female");

        nokGenderComboBox.getItems().addAll("Female", "Male");
        nokGenderComboBox.setValue("Female");

        incomeSourceTypeComboBox.getItems().addAll("EMPLOYMENT", "ALLOWANCE", "PENSION", "INVESTMENT", "OTHER", "BUSINESS");
        incomeSourceTypeComboBox.setValue("EMPLOYMENT");

        nokRelationshipComboBox.getItems().addAll("Mother", "Father", "Sister", "Brother", "Husband", "Wife", "Relative", "Other");
        nokRelationshipComboBox.setValue("Mother");

        accountTypeComboBox.getItems().addAll("Savings", "Cheque", "Investment");
        accountTypeComboBox.setValue("Savings");

        accountTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAccountRequirements(newVal));
        updateAccountRequirements("Savings");
    }

    private void updateDashboard() {
        if (currentTeller != null) {
            welcomeLabel.setText("Personal Customer Account Opening - " + currentTeller.getFullName() +
                    " | " + currentTeller.getTellerId());
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
            // --- Validate all sections first ---
            if (!validateIndividualInformation()) return;
            if (!validateNextOfKinInformation()) return;
            if (!validateIncomeInformation()) return;
            if (!validateDepositAmount(accountTypeComboBox.getValue())) return;

            // --- Parse DOB ---
            LocalDate dobLocalDate = customerDobField.getValue();
            if (dobLocalDate == null) {
                showAlert("Invalid DOB", "Please select a valid date of birth.");
                return;
            }
            Date dateOfBirth = Date.from(dobLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // --- Parse initial deposit ---
            double initialDeposit;
            try {
                initialDeposit = Double.parseDouble(initialDepositField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert("Invalid Deposit", "Please enter a valid numeric value for the initial deposit.");
                return;
            }

            // --- Map ComboBox selection to proper account type string ---
            String selectedAccountType = accountTypeComboBox.getValue(); // "Savings", "Cheque", "Investment"
            String accountTypeCode;
            switch (selectedAccountType) {
                case "Savings" -> accountTypeCode = "savings";
                case "Cheque" -> accountTypeCode = "cheque";
                case "Investment" -> accountTypeCode = "investment";
                default -> {
                    showAlert("Account Error", "Invalid account type selected.");
                    return;
                }
            }

            if (!validateMinimumDeposit(selectedAccountType, initialDeposit)) return;

            Individual customer = new Individual(
                    null,
                    null,
                    customerAddressField.getText().trim(),
                    customerEmailField.getText().trim().isEmpty() ? "Not Provided" : customerEmailField.getText().trim(),
                    customerFirstNameField.getText().trim(),
                    customerSurnameField.getText().trim(),
                    customerIdField.getText().trim(),
                    dateOfBirth,
                    customerGenderComboBox.getValue(),
                    customerPhoneField.getText().trim(),
                    nokNameField.getText().trim(),
                    nokRelationshipComboBox.getValue(),
                    nokGenderComboBox.getValue(),
                    nokPhoneField.getText().trim(),
                    incomeSourceTypeComboBox.getValue(),
                    employerNameField.getText().trim(),
                    employerAddressField.getText().trim(),
                    Double.parseDouble(monthlyIncomeField.getText().trim())
            );


            // --- Generate login credentials ---
            LoginService loginService = new LoginService();
            customer.setUsername(loginService.generateConsistentUsername(customer));
            customer.setPassword(loginService.generateSecurePassword());

            // --- Save customer ---
            bankingService.createCustomer(customer);

            // --- Open account via BankingService ---
            Account newAccount = bankingService.createAccount(
                    currentTeller,
                    customer,
                    accountTypeCode,
                    currentTeller.getBranchCode(),
                    initialDeposit
            );

            if (newAccount != null) {
                showSuccessAlert(customer, newAccount, initialDeposit);
            } else {
                showAlert("Account Opening Failed", "Account could not be created.");
            }

        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values for deposit and income amounts.");
        } catch (Exception e) {
            showAlert("Error", "Error opening account: " + e.getMessage());
        }
    }

    private void showSuccessAlert(com.example.bankaccount.Individual customer, com.example.bankaccount.Account newAccount, double initialDeposit) {
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
        if (customerFirstNameField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter first name"); return false; }
        if (customerSurnameField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter surname"); return false; }
        if (customerIdField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter ID number"); return false; }
        if (customerDobField.getValue() == null) {showAlert("Validation Error", "Enter date of birth");return false;}
        if (customerAddressField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter address"); return false; }
        if (customerPhoneField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter phone number"); return false; }
        return true;
    }

    private boolean validateNextOfKinInformation() {
        if (nokNameField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter next of kin name"); return false; }
        if (nokPhoneField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter next of kin phone"); return false; }
        return true;
    }

    private boolean validateIncomeInformation() {
        if (monthlyIncomeField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter monthly income"); return false; }
        if (employerNameField.getText().trim().isEmpty()) { showAlert("Validation Error", "Enter employer/provider name"); return false; }
        try {
            double income = Double.parseDouble(monthlyIncomeField.getText().trim());
            if (income <= 0) { showAlert("Validation Error", "Income must be > 0"); return false; }
        } catch (NumberFormatException e) { showAlert("Validation Error", "Invalid income value"); return false; }
        return true;
    }

    private boolean validateDepositAmount(String accountType) {
        if (accountType.equals("Cheque")) {

            if (initialDepositField.getText().trim().isEmpty()) {
                initialDepositField.setText("0");
                return true;
            }

            try {
                double deposit = Double.parseDouble(initialDepositField.getText().trim());
                if (deposit < 0) {
                    showAlert("Validation Error", "Deposit cannot be negative.");
                    return false;
                }
                return true; // zero or above is OK
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Invalid deposit value");
                return false;
            }

        }

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
            showAlert("Validation Error", String.format("Minimum deposit for %s account is P%.2f", accountType, minimum));
            initialDepositField.requestFocus();
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        customerFirstNameField.clear();
        customerSurnameField.clear();
        customerIdField.clear();
        customerDobField.setValue(null);
        customerAddressField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        nokNameField.clear();
        nokPhoneField.clear();
        monthlyIncomeField.clear();
        employerNameField.clear();
        employerAddressField.clear();
        initialDepositField.clear();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tellerLanding.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
