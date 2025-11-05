package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private TextField usernameField;
    @FXML private TextField idNumberField;
    @FXML private TextField emailField;
    @FXML private Label securityQuestionLabel;
    @FXML private TextField securityAnswerField;
    @FXML private Button resetButton;
    @FXML private Button cancelButton;

    private CustomerDAO customerDAO;

    public void setCustomerDAO(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    @FXML
    private void initialize() {
        // Disable security fields until user is verified
        securityQuestionLabel.setDisable(true);
        securityAnswerField.setDisable(true);
        resetButton.setDisable(true);
    }

    @FXML
    private void handleVerifyUser() {
        String username = usernameField.getText();
        String idNumber = idNumberField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || idNumber.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        // Find customer by username
        Customer customer = findCustomerByUsername(username);
        if (customer == null) {
            showAlert("Error", "User not found");
            return;
        }

        // Verify ID number and email
        if (!customer.getIdentificationNumber().equals(idNumber) ||
                !customer.getContactEmail().equalsIgnoreCase(email)) {
            showAlert("Error", "Invalid credentials");
            return;
        }

        // User verified - enable security question
        securityQuestionLabel.setDisable(false);
        securityAnswerField.setDisable(false);
        resetButton.setDisable(false);

        // Set security question (you can customize this)
        securityQuestionLabel.setText("Security: What is your mother's maiden name?");

        showAlert("Success", "User verified. Please answer security question.");
    }

    @FXML
    private void handleResetPassword() {
        String securityAnswer = securityAnswerField.getText();

        if (securityAnswer.isEmpty()) {
            showAlert("Error", "Please answer security question");
            return;
        }

        // Simple security check (in real app, use proper security questions)
        if (securityAnswer.equalsIgnoreCase("smith")) { // Example answer
            // Generate temporary password
            String tempPassword = generateTemporaryPassword();

            // In real app, you would:
            // 1. Update the customer's password in database
            // 2. Send email with temporary password

            showAlert("Password Reset",
                    "Your temporary password is: " + tempPassword +
                            "\nPlease change it after login.");

            // Close the window
            closeWindow();
        } else {
            showAlert("Error", "Incorrect security answer");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private Customer findCustomerByUsername(String username) {
        // You'll need to add this method to your CustomerDAO
        for (Customer customer : customerDAO.findAllCustomers()) {
            if (customer.getUsername() != null &&
                    customer.getUsername().equalsIgnoreCase(username)) {
                return customer;
            }
        }
        return null;
    }

    private String generateTemporaryPassword() {
        // Generate a random 8-character temporary password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}