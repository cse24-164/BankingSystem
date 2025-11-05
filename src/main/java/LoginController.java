package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private LoginService loginService = new LoginService();

    @FXML
    private void initialize() {
        System.out.println("LoginController initialized");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password");
            return;
        }

        System.out.println("🔐 Attempting login for: " + username);

        AuthContext authContext = loginService.login(username, password);

        if (authContext != null) {
            handleSuccessfulLogin(authContext);
        } else {
            showAlert("Login Failed", "Invalid username or password");
            passwordField.clear();
        }
    }

    private void handleSuccessfulLogin(AuthContext authContext) {
        try {
            if (authContext.isBankTeller()) {
                BankTeller teller = authContext.getBankTeller();
                System.out.println("✅ Teller login successful: " + teller.getFullName());
                openTellerDashboard(teller);

            } else if (authContext.isCustomer()) {
                // Open customer dashboard
                Customer customer = authContext.getCustomer();
                System.out.println("✅ Customer login successful: " + customer.getDisplayName());
                openCustomerDashboard(customer);
            }

        } catch (Exception e) {
            System.err.println("❌ Error handling successful login: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("forgotPassword.fxml"));
            Parent root = loader.load();

            ForgotPasswordController controller = loader.getController();
            controller.setCustomerDAO(new FileCustomerDAO());

            Stage stage = new Stage();
            stage.setTitle("Forgot Password");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            System.out.println("Error opening forgot password screen: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot open password reset screen");
        }
    }

    @FXML
    private void handleCancel() {
        System.exit(0);
    }


    private void openTellerDashboard(BankTeller teller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tellerLanding.fxml"));
            Parent root = loader.load();

            TellerLandingController controller = loader.getController();
            controller.setBankTeller(teller);

            Stage stage = new Stage();
            stage.setTitle("Bank Teller Portal - " + teller.getFullName());
            stage.setScene(new Scene(root, 1024, 768));
            stage.setResizable(false);
            stage.show();

            // Close login window
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            showAlert("Error", "Failed to open teller dashboard: " + e.getMessage());
        }
    }

    private void openCustomerDashboard(Customer customer) {
        try {
            System.out.println("Attempting to load customer dashboard...");

            URL fxmlUrl = getClass().getResource("customerDashboard.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ FXML file not found: customerDashboard.fxml");
                showAlert("Error", "Dashboard file not found: customerDashboard.fxml");
                return;
            }
            System.out.println("✅ FXML file found: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("✅ FXML loaded successfully");

            CustomerDashboardController controller = loader.getController();
            System.out.println("✅ Controller loaded: " + controller);

            controller.setCustomer(customer);
            System.out.println("✅ Customer set in controller");

            Stage customerStage = new Stage();
            String displayName = customer.getDisplayName();
            customerStage.setTitle("Customer Portal - " + displayName);
            customerStage.setScene(new Scene(root, 1000, 700)); // Match FXML dimensions
            customerStage.setResizable(false);

            customerStage.setOnCloseRequest(event -> {
                System.out.println("Customer dashboard closed");
                // close window
            });

            System.out.println("✅ Showing customer dashboard stage");
            customerStage.show();

            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
            System.out.println("✅ Login window closed, application should continue running");

        } catch (Exception e) {
            System.err.println("❌ Error opening customer dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open customer dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

