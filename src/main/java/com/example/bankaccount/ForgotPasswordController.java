package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private TextField usernameField;
    @FXML private Button resetButton;
    @FXML private Button cancelButton;

    private com.example.bankaccount.LoginService loginService;

    public void setLoginService(com.example.bankaccount.LoginService loginService) {
        this.loginService = loginService;
    }

    @FXML
    private void handleResetPassword() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert("Error", "Please enter your username");
            return;
        }

        try {
            String newPassword = loginService.resetPasswordForUsername(username);
            showAlert("Password Reset",
                    "Your password has been reset.\nTemporary password: " + newPassword);
            closeWindow();
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }


    @FXML
    private void handleCancel() {
        closeWindow();
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
