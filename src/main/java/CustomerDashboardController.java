package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.util.Callback;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import java.util.List;

public class CustomerDashboardController {

    // Fields from FXML
    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private ListView<Account> accountsListView;
    @FXML private ListView<String> transactionsListView;
    @FXML private ComboBox<Account> transferFromCombo;
    @FXML private TextField targetAccountField;
    @FXML private TextField transferAmountField;
    @FXML private TextField transferDescription;

    private Customer currentCustomer;
    private BankingService bankingService;

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        this.bankingService = new BankingService(); // Initialize banking service
        updateDashboard();
    }

    private void updateDashboard() {
        if (currentCustomer != null) {
            welcomeLabel.setText("Welcome, " + currentCustomer.getDisplayName());
            updateAccountDisplay();
            updateBalanceSummary();
            updateTransferComboBox();
            updateTransactionHistory();
        }
    }

    private void updateAccountDisplay() {
        accountsListView.getItems().clear();

        // Use BankingService to get customer accounts (loaded from file)
        List<Account> customerAccounts = bankingService.getCustomerAccounts(currentCustomer.getIdentificationNumber());

        if (customerAccounts.isEmpty()) {
            // Create a custom cell factory to display message when no accounts
            accountsListView.setCellFactory(lv -> new ListCell<Account>() {
                @Override
                protected void updateItem(Account item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("No accounts available");
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    } else {
                        setText(null);
                    }
                }
            });
            return;
        }

        accountsListView.setCellFactory(lv -> new ListCell<Account>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%s - %s\nBalance: P%.2f\nBranch: %s",
                            account.getAccountNumber(),
                            getAccountTypeDisplay(account),
                            account.getBalance(),
                            account.getBranch()));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });

        accountsListView.getItems().addAll(customerAccounts);
    }

    private void updateBalanceSummary() {
        List<Account> customerAccounts = bankingService.getCustomerAccounts(currentCustomer.getIdentificationNumber());
        double totalBalance = customerAccounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
        balanceLabel.setText("Total Balance: P" + String.format("%.2f", totalBalance));

        // Color code based on balance
        if (totalBalance == 0) {
            balanceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (totalBalance < 1000) {
            balanceLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            balanceLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    private void updateTransactionHistory() {
        transactionsListView.getItems().clear();

        // Get all accounts for this customer
        List<Account> customerAccounts = bankingService.getCustomerAccounts(currentCustomer.getIdentificationNumber());

        if (customerAccounts.isEmpty()) {
            transactionsListView.getItems().add("No transactions available");
            return;
        }

        // Show recent transactions from all accounts
        int transactionCount = 0;
        for (Account account : customerAccounts) {
            List<Transaction> accountTransactions = bankingService.getAccountTransactions(account.getAccountNumber());

            for (Transaction transaction : accountTransactions) {
                String transactionText = String.format("Acc #%d: %s - P%.2f (Bal: P%.2f)\n%s",
                        transaction.getAccountNumber(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getBalance(),
                        transaction.getDescription());
                transactionsListView.getItems().add(transactionText);
                transactionCount++;

                // Limit to last 10 transactions total
                if (transactionCount >= 10) break;
            }
            if (transactionCount >= 10) break;
        }

        if (transactionsListView.getItems().isEmpty()) {
            transactionsListView.getItems().add("No transactions found");
        }
    }

    private void updateTransferComboBox() {
        transferFromCombo.getItems().clear();

        // Use BankingService to get accounts
        List<Account> customerAccounts = bankingService.getCustomerAccounts(currentCustomer.getIdentificationNumber());

        transferFromCombo.setCellFactory(lv -> new ListCell<Account>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s (P%.2f)",
                            account.getAccountNumber(),
                            getAccountTypeDisplay(account),
                            account.getBalance()));
                }
            }
        });

        transferFromCombo.setButtonCell(new ListCell<Account>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText("Select Account");
                } else {
                    setText(String.format("%s - %s",
                            account.getAccountNumber(),
                            getAccountTypeDisplay(account)));
                }
            }
        });

        transferFromCombo.getItems().addAll(customerAccounts);

        if (!transferFromCombo.getItems().isEmpty()) {
            transferFromCombo.setValue(transferFromCombo.getItems().get(0));
        }
    }

    private String getAccountTypeDisplay(Account account) {
        if (account instanceof SavingsAccount) {
            return "Savings Account";
        } else if (account instanceof ChequeAccount) {
            return "Cheque Account";
        } else if (account instanceof InvestmentAccount) {
            return "Investment Account";
        } else {
            return "Account";
        }
    }

    @FXML
    private void handleDeposit() {
        // Get selected account from ListView
        Account selectedAccount = accountsListView.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Error", "Please select an account from the list");
            return;
        }

        // Create deposit dialog
        Dialog<Double> depositDialog = new Dialog<>();
        depositDialog.setTitle("Deposit Funds");
        depositDialog.setHeaderText("Deposit to Account #" + selectedAccount.getAccountNumber());

        // Set the button types
        ButtonType depositButtonType = new ButtonType("Deposit", ButtonBar.ButtonData.OK_DONE);
        depositDialog.getDialogPane().getButtonTypes().addAll(depositButtonType, ButtonType.CANCEL);

        // Create the amount input
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label currentBalanceLabel = new Label("Current Balance: P" + String.format("%.2f", selectedAccount.getBalance()));
        currentBalanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label amountLabel = new Label("Deposit Amount:");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        Label descriptionLabel = new Label("Description (Optional):");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g., Cash deposit, Transfer from...");

        content.getChildren().addAll(currentBalanceLabel, amountLabel, amountField, descriptionLabel, descriptionField);

        depositDialog.getDialogPane().setContent(content);

        // Enable/disable deposit button based on input
        Node depositButton = depositDialog.getDialogPane().lookupButton(depositButtonType);
        depositButton.setDisable(true);

        // Add validation
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double amount = newValue.isEmpty() ? 0 : Double.parseDouble(newValue);
                depositButton.setDisable(amount <= 0);
            } catch (NumberFormatException e) {
                depositButton.setDisable(true);
            }
        });

        // Convert result to amount
        depositDialog.setResultConverter(dialogButton -> {
            if (dialogButton == depositButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        // Show dialog and process result
        depositDialog.showAndWait().ifPresent(amount -> {
            if (amount > 0) {
                try {
                    String description = descriptionField.getText().isEmpty() ?
                            "Customer self-service deposit" : descriptionField.getText();

                    // Use BankingService for persistence
                    bankingService.deposit(selectedAccount.getAccountNumber(), amount, description);

                    // Refresh the dashboard
                    updateDashboard();

                    showAlert("Success",
                            String.format("✅ Deposited P%.2f to account %s\nNew Balance: P%.2f",
                                    amount,
                                    selectedAccount.getAccountNumber(),
                                    bankingService.getAccountBalance(selectedAccount.getAccountNumber())));

                } catch (Exception e) {
                    showAlert("Error", "Deposit failed: " + e.getMessage());
                }
            } else {
                showAlert("Error", "Amount must be positive");
            }
        });
    }

    @FXML
    private void handleWithdraw() {
        // Get selected account from ListView
        Account selectedAccount = accountsListView.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showAlert("Error", "Please select an account from the list");
            return;
        }

        // Create withdrawal dialog
        Dialog<Double> withdrawDialog = new Dialog<>();
        withdrawDialog.setTitle("Withdraw Funds");
        withdrawDialog.setHeaderText("Withdraw from Account #" + selectedAccount.getAccountNumber());

        // Set the button types
        ButtonType withdrawButtonType = new ButtonType("Withdraw", ButtonBar.ButtonData.OK_DONE);
        withdrawDialog.getDialogPane().getButtonTypes().addAll(withdrawButtonType, ButtonType.CANCEL);

        // Create the amount input
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label currentBalanceLabel = new Label("Current Balance: P" + String.format("%.2f", selectedAccount.getBalance()));
        currentBalanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label amountLabel = new Label("Withdrawal Amount:");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        Label descriptionLabel = new Label("Description (Optional):");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g., Cash withdrawal, Payment to...");

        content.getChildren().addAll(currentBalanceLabel, amountLabel, amountField, descriptionLabel, descriptionField);

        withdrawDialog.getDialogPane().setContent(content);

        // Enable/disable withdraw button based on input
        Node withdrawButton = withdrawDialog.getDialogPane().lookupButton(withdrawButtonType);
        withdrawButton.setDisable(true);

        // Add validation
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double amount = newValue.isEmpty() ? 0 : Double.parseDouble(newValue);
                boolean valid = amount > 0 && amount <= selectedAccount.getBalance();
                withdrawButton.setDisable(!valid);
            } catch (NumberFormatException e) {
                withdrawButton.setDisable(true);
            }
        });

        // Convert result to amount
        withdrawDialog.setResultConverter(dialogButton -> {
            if (dialogButton == withdrawButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        // Show dialog and process result
        withdrawDialog.showAndWait().ifPresent(amount -> {
            if (amount > 0) {
                try {
                    String description = descriptionField.getText().isEmpty() ?
                            "Customer self-service withdrawal" : descriptionField.getText();

                    // Use BankingService for persistence
                    bankingService.withdraw(selectedAccount.getAccountNumber(), amount, description);

                    // Refresh the dashboard
                    updateDashboard();

                    showAlert("Success",
                            String.format("✅ Withdrew P%.2f from account %s\nNew Balance: P%.2f",
                                    amount,
                                    selectedAccount.getAccountNumber(),
                                    bankingService.getAccountBalance(selectedAccount.getAccountNumber())));

                } catch (Exception e) {
                    showAlert("Error", "Withdrawal failed: " + e.getMessage());
                }
            } else {
                showAlert("Error", "Amount must be positive");
            }
        });
    }

    @FXML
    private void handleTransfer() {
        try {
            Account fromAccount = transferFromCombo.getValue();
            if (fromAccount == null) {
                showAlert("Error", "Please select source account");
                return;
            }

            String toAccountNumberStr = targetAccountField.getText().trim();
            if (toAccountNumberStr.isEmpty()) {
                showAlert("Error", "Please enter target account number");
                return;
            }

            int toAccountNumber;
            try {
                toAccountNumber = Integer.parseInt(toAccountNumberStr);
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid account number");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(transferAmountField.getText());
                if (amount <= 0) {
                    showAlert("Error", "Amount must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid amount");
                return;
            }

            // Check if target account exists
            Account toAccount = bankingService.getAccount(toAccountNumber);
            if (toAccount == null) {
                showAlert("Error", "Target account #" + toAccountNumber + " not found");
                return;
            }

            // Check sufficient funds
            if (fromAccount.getBalance() < amount) {
                showAlert("Error", "Insufficient funds for transfer");
                return;
            }

            // Perform transfer using BankingService
            String description = transferDescription.getText().isEmpty() ?
                    "Transfer to account #" + toAccountNumber : transferDescription.getText();

            // Withdraw from source account
            bankingService.withdraw(fromAccount.getAccountNumber(), amount, description);

            // Deposit to target account
            bankingService.deposit(toAccountNumber, amount, "Transfer from account #" + fromAccount.getAccountNumber());

            // Refresh dashboard
            updateDashboard();

            showAlert("Success",
                    String.format("✅ Transferred P%.2f from account #%d to account #%d",
                            amount, fromAccount.getAccountNumber(), toAccountNumber));

            // Clear form
            targetAccountField.clear();
            transferAmountField.clear();
            transferDescription.clear();

        } catch (Exception e) {
            showAlert("Error", "Transfer failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        updateDashboard();
        showAlert("Info", "Dashboard refreshed with latest data from files");
    }

    @FXML
    private void handleViewProfile() {
        try {
            Dialog<Void> profileDialog = new Dialog<>();
            profileDialog.setTitle("Customer Profile - " + currentCustomer.getDisplayName());
            profileDialog.setHeaderText("📋 PROFILE INFORMATION");
            profileDialog.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");

            VBox profileContent = new VBox(10);
            profileContent.setPadding(new Insets(20));
            profileContent.setStyle("-fx-background-color: white;");

            StringBuilder profileInfo = new StringBuilder();
            profileInfo.append("=== CUSTOMER PROFILE ===\n\n");

            if (currentCustomer instanceof Individual) {
                Individual individual = (Individual) currentCustomer;
                profileInfo.append("👤 INDIVIDUAL CUSTOMER\n");
                profileInfo.append("────────────────────\n");
                profileInfo.append("First Name: ").append(individual.getFirstName()).append("\n");
                profileInfo.append("Surname: ").append(individual.getSurname()).append("\n");
                profileInfo.append("ID Number: ").append(individual.getIdNumber()).append("\n");
                profileInfo.append("Date of Birth: ").append(individual.getDateOfBirth()).append("\n");

                if (individual.getNextOfKin() != null) {
                    profileInfo.append("\n👥 NEXT OF KIN\n");
                    profileInfo.append("───────────\n");
                    profileInfo.append("Name: ").append(individual.getNextOfKin().getFullName()).append("\n");
                    profileInfo.append("Relationship: ").append(individual.getNextOfKin().getRelationship()).append("\n");
                    profileInfo.append("Phone: ").append(individual.getNextOfKin().getPhoneNumber()).append("\n");
                    profileInfo.append("Email: ").append(individual.getNextOfKin().getEmail()).append("\n");
                    profileInfo.append("Address: ").append(individual.getNextOfKin().getAddress()).append("\n");
                }

            } else if (currentCustomer instanceof Company) {
                Company company = (Company) currentCustomer;
                profileInfo.append("🏢 COMPANY CUSTOMER\n");
                profileInfo.append("────────────────\n");
                profileInfo.append("Company Name: ").append(company.getCompanyName()).append("\n");
                profileInfo.append("Registration: ").append(company.getRegistrationNumber()).append("\n");
                profileInfo.append("Business Type: ").append(company.getBusinessType()).append("\n");
                profileInfo.append("Contact Person: ").append(company.getContactPersonName()).append("\n");
                profileInfo.append("Annual Revenue: P").append(String.format("%.2f", company.getAnnualRevenue())).append("\n");
            }

            profileInfo.append("\n📞 CONTACT INFORMATION\n");
            profileInfo.append("───────────────────\n");
            profileInfo.append("Email: ").append(currentCustomer.getContactEmail()).append("\n");
            profileInfo.append("Phone: ").append(currentCustomer.getContactPhone()).append("\n");
            profileInfo.append("Address: ").append(currentCustomer.getAddress()).append("\n");
            profileInfo.append("Member Since: ").append(currentCustomer.getRegistrationDate()).append("\n");

            profileInfo.append("\n💰 ACCOUNT SUMMARY\n");
            profileInfo.append("───────────────\n");

            List<Account> customerAccounts = bankingService.getCustomerAccounts(currentCustomer.getIdentificationNumber());
            profileInfo.append("Total Accounts: ").append(customerAccounts.size()).append("\n");

            double totalBalance = customerAccounts.stream()
                    .mapToDouble(Account::getBalance)
                    .sum();
            profileInfo.append("Total Balance: P").append(String.format("%.2f", totalBalance)).append("\n");

            // List all accounts
            profileInfo.append("\n📊 ACCOUNTS:\n");
            profileInfo.append("─────────\n");
            for (Account account : customerAccounts) {
                profileInfo.append(String.format("- %s (%s): P%.2f\n",
                        account.getAccountNumber(),
                        getAccountTypeDisplay(account),
                        account.getBalance()));
            }

            TextArea profileText = new TextArea(profileInfo.toString());
            profileText.setEditable(false);
            profileText.setWrapText(true);
            profileText.setPrefSize(500, 400);
            profileText.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-background-color: #f8f9fa;");

            profileContent.getChildren().add(profileText);
            profileDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            profileDialog.getDialogPane().setContent(profileContent);
            profileDialog.showAndWait();

        } catch (Exception e) {
            showAlert("Error", "Failed to display profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Logout");
            confirmAlert.setHeaderText("Logout Confirmation");
            confirmAlert.setContentText("Are you sure you want to logout?");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainmenu.fxml"));
                Parent root = loader.load();

                Stage mainMenuStage = new Stage();
                mainMenuStage.setTitle("Banking System - Main Menu");
                mainMenuStage.setScene(new Scene(root, 800, 600));
                mainMenuStage.setResizable(false);
                mainMenuStage.show();

                Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
                currentStage.close();
            }
        } catch (Exception e) {
            showAlert("Error", "Error during logout: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}