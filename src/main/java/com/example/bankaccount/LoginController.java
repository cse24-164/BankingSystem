package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import javafx.scene.control.Button;
import javafx.scene.Node;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private LoginService loginService = new LoginService();
    private Customer loggedInCustomer;

    @FXML
    private void initialize() {
        System.out.println("LoginController initialized");
    }

    @FXML private Button loginButton;

    @FXML
    private void handleLogin(ActionEvent event) {
        loginButton.setDisable(true); // prevent multiple clicks

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password");
            loginButton.setDisable(false);
            return;
        }

        System.out.println("Attempting login for: " + username);

        AuthContext authContext = loginService.login(username, password);

        if (authContext != null) {
            handleSuccessfulLogin(authContext, event);
        } else {
            showAlert("Login Failed", "Invalid username or password");
            passwordField.clear();
            loginButton.setDisable(false);
        }
    }

    private void handleSuccessfulLogin(AuthContext authContext, ActionEvent event) {
        try {
            if (authContext.isBankTeller()) {
                BankTeller teller = authContext.getBankTeller();
                System.out.println(" Teller login successful: " + teller.getFullName());
                openTellerDashboard(teller, event);

            } else if (authContext.isCustomer()) {
                loggedInCustomer = authContext.getCustomer();
                openCustomerDashboard(loggedInCustomer, event);
            }

        } catch (Exception e) {
            System.err.println(" Error handling successful login: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open dashboard: " + e.getMessage());
        }
    }

    private void openTellerDashboard(BankTeller teller, ActionEvent event) {
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
            Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            showAlert("Error", "Failed to open teller dashboard: " + e.getMessage());
        }
    }

    private void openCustomerDashboard(Customer customer, ActionEvent event) {
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

            customerStage.setOnCloseRequest(windowEvent -> {
                System.out.println("Customer dashboard closed");
            });

            System.out.println("✅ Showing customer dashboard stage");
            customerStage.show();

            // Close login window
            Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loginStage.close();
            System.out.println("Login window closed, application should continue running");

        } catch (Exception e) {
            System.err.println(" Error opening customer dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open customer dashboard: " + e.getMessage());
        }
    }

    public void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("forgotPassword.fxml"));
            Parent root = loader.load();

            com.example.bankaccount.ForgotPasswordController controller = loader.getController();

            controller.setLoginService(new com.example.bankaccount.LoginService());

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow(Button button) {
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }


}

