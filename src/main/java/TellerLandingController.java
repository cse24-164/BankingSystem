package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TellerLandingController {

    @FXML private Label welcomeLabel;
    @FXML private Label branchLabel;
    @FXML private Label timeLabel;

    private BankTeller currentTeller;
    private int accountsOpenedToday = 0;
    private int customersCreatedToday = 0;

    public void setBankTeller(BankTeller teller) {
        this.currentTeller = teller;
        updateDashboard();
        startClock();
    }

    private void updateDashboard() {
        if (currentTeller != null) {
            welcomeLabel.setText("Welcome, " + currentTeller.getFirstName());
            branchLabel.setText("Branch: " + currentTeller.getBranchCode());
        }
    }

    private void startClock() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    String time = timeFormat.format(new Date());
                    javafx.application.Platform.runLater(() -> timeLabel.setText(time));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    @FXML
    private void openIndividualAccount() {
        openAccountScreen("individualAccount.fxml", "Open Individual Account", IndividualAccountController.class);
    }

    @FXML
    private void openCompanyAccount() {
        openAccountScreen("companyAccount.fxml", "Open Company Account", CompanyAccountController.class);
    }

    @FXML
    private void viewAccountHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("accountHistory.fxml"));
            Parent root = loader.load();

            AccountHistoryController controller = loader.getController();
            controller.setBankTeller(currentTeller);

            AccountDAO accountDAO = new JDBCAccountDAO();
            controller.setAccountDAO(accountDAO);

            Stage stage = new Stage();
            stage.setTitle("Account History - " + currentTeller.getFirstName());
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Cannot open account history: " + e.getMessage());
        }
    }

    private <T> void openAccountScreen(String fxmlFile, String title, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof IndividualAccountController) {
                ((IndividualAccountController) controller).setBankTeller(currentTeller);
                ((IndividualAccountController) controller).setLandingController(this);
            } else if (controller instanceof CompanyAccountController) {
                ((CompanyAccountController) controller).setBankTeller(currentTeller);
                ((CompanyAccountController) controller).setLandingController(this);
            }

            Stage stage = new Stage();
            stage.setTitle(title + " - " + currentTeller.getFullName());
            stage.setScene(new Scene(root, 1024, 768));
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Cannot open " + title.toLowerCase() + " screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
    }

    public void addAccountOpened(String customerName, String accountType) {
        accountsOpenedToday++;
    }

    public void addCustomerCreated(String customerName) {
        customersCreatedToday++;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}