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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;

import com.example.bankaccount.ExistingCustomerController;


public class TellerLandingController {

    @FXML private Label welcomeLabel;
    @FXML private Label branchLabel;
    @FXML private Label timeLabel;

    private BankTeller currentTeller;
    private int accountsOpenedToday = 0;
    private int customersCreatedToday = 0;

    private BankingService bankingService;

    public void setBankTeller(BankTeller teller) {
        this.currentTeller = teller;
        this.bankingService = new BankingService(); // <-- initialize it here
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
    private void openAccountForExistingCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("existingCustomerOverview.fxml"));
            Parent root = loader.load();

            ExistingCustomerController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Select Existing Customer");
            stage.setScene(new Scene(root, 600, 400));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // ---- VALIDATE SELECTION ----
            Customer selectedCustomer = controller.getSelectedCustomer();
            if (selectedCustomer == null) {
                showAlert("Error", "Please select a customer first.");
                return;
            }

            // ---- INITIAL DEPOSIT INPUT ----
            TextInputDialog depositDialog = new TextInputDialog("50.0");
            depositDialog.setTitle("Open Account for Existing Customer");
            depositDialog.setHeaderText("Enter initial deposit for " + selectedCustomer.getDisplayName());
            depositDialog.setContentText("Amount:");

            depositDialog.showAndWait().ifPresent(input -> {
                try {
                    double initialDeposit = Double.parseDouble(input);

                    // ---- ACCOUNT TYPE INPUT ----
                    ChoiceDialog<String> accountTypeDialog = new ChoiceDialog<>(
                            "savings", "savings", "cheque", "investment"
                    );
                    accountTypeDialog.setTitle("Select Account Type");
                    accountTypeDialog.setHeaderText("Select account type for " + selectedCustomer.getDisplayName());
                    accountTypeDialog.setContentText("Account Type:");

                    accountTypeDialog.showAndWait().ifPresent(accountType -> {
                        try {
                            Account newAccount = bankingService.createAccountForExistingCustomer(
                                    currentTeller,
                                    selectedCustomer,
                                    accountType,
                                    currentTeller.getBranchCode(),
                                    initialDeposit
                            );

                            showAlert(
                                    "Success",
                                    "Account created:\n" +
                                            "Account No: " + newAccount.getAccountNumber() + "\n" +
                                            "Customer: " + selectedCustomer.getDisplayName() + "\n" +
                                            "Balance: P" + String.format("%.2f", newAccount.getBalance())
                            );

                        } catch (Exception e) {
                            showAlert("Error", "Failed to create account: " + e.getMessage());
                        }
                    });

                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter a valid numeric deposit amount.");
                }
            });

        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
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