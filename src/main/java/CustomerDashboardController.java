package com.example.bankaccount;
import com.example.bankaccount.Customer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.util.List;

public class CustomerDashboardController {

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
    private Customer loggedInCustomer;
    private JDBCAccountDAO accountDAO = new JDBCAccountDAO();

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        this.bankingService = new BankingService();

        List<Account> accounts = bankingService.getCustomerAccounts(customer.getCustomerId());
        customer.setAccounts(accounts);

        refreshDashboard();
    }

    private void refreshDashboard() {
        if (currentCustomer == null) return;

        welcomeLabel.setText("Welcome, " + currentCustomer.getDisplayName());
        updateAccountsList();
        updateBalance();
        updateTransferCombo();
        updateTransactionsList();
    }

    private void updateAccountsList() {
        if (currentCustomer == null) return;

        List<Account> accounts = accountDAO.findAccountsByCustomer(currentCustomer.getCustomerId());
        accountsListView.getItems().setAll(accounts);

        accountsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);

                if (empty || account == null) {
                    setText("No accounts available");
                    setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray; -fx-font-style: italic;");
                } else {
                    setText(String.format("%s - %s\nBalance: P%.2f\nBranch: %s",
                            account.getAccountNumber(),
                            getAccountTypeDisplay(account),
                            account.getBalance(),
                            account.getBranch()));
                    setStyle("-fx-background-color: #e0e0e0; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10px;");
                }
            }
        });
    }

    private void updateBalance() {
        double total = bankingService.getCustomerAccounts(currentCustomer.getCustomerId())
                .stream()
                .mapToDouble(Account::getBalance)
                .sum();
        balanceLabel.setText("Total Balance: P" + String.format("%.2f", total));

        String color = total == 0 ? "#e74c3c" : total < 1000 ? "#f39c12" : "#bca014";
        balanceLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void updateTransferCombo() {
        List<Account> accounts = accountDAO.findAccountsByCustomer(currentCustomer.getCustomerId());
        transferFromCombo.getItems().setAll(accounts);
        transferFromCombo.setValue(accounts.isEmpty() ? null : accounts.get(0));

        transferFromCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                setText(empty || account == null ? null :
                        String.format("%s - %s (P%.2f)",
                                account.getAccountNumber(),
                                getAccountTypeDisplay(account),
                                account.getBalance()));
            }
        });

        transferFromCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                setText(empty || account == null ? "Select Account" :
                        String.format("%s - %s",
                                account.getAccountNumber(),
                                getAccountTypeDisplay(account)));
            }
        });
    }

    private void updateTransactionsList() {
        transactionsListView.getItems().clear();
        List<Account> accounts = accountDAO.findAccountsByCustomer(currentCustomer.getCustomerId());
        int count = 0;

        for (Account account : accounts) {
            List<Transaction> transactions = bankingService.getAccountTransactions(account.getAccountNumber());
            for (Transaction t : transactions) {
                if (count >= 10) break;
                transactionsListView.getItems().add(String.format(
                        "Acc #%s: %s - P%.2f (Bal: P%.2f)\n%s",
                        t.getAccountNumber(),
                        t.getTransactionType(),
                        t.getAmount(),
                        t.getBalance(),
                        t.getDescription()
                ));
                count++;
            }
            if (count >= 10) break;
        }

        if (transactionsListView.getItems().isEmpty())
            transactionsListView.getItems().add("No transactions found");
    }

    private String getAccountTypeDisplay(Account account) {
        if (account instanceof SavingsAccount) return "Savings Account";
        if (account instanceof ChequeAccount) return "Cheque Account";
        if (account instanceof InvestmentAccount) return "Investment Account";
        return "Account";
    }

    // Generic method for deposit or withdrawal
    private void showTransactionDialog(Account account, String type) {
        if (account == null) {
            showAlert("Error", "Please select an account first");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle(type + " Funds");
        dialog.setHeaderText(type + " to Account #" + account.getAccountNumber());

        ButtonType actionButtonType = new ButtonType(type, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label balanceLabel = new Label("Current Balance: P" + String.format("%.2f", account.getBalance()));
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description (optional)");

        content.getChildren().addAll(balanceLabel, new Label("Amount:"), amountField,
                new Label("Description:"), descriptionField);
        dialog.getDialogPane().setContent(content);

        Node actionButton = dialog.getDialogPane().lookupButton(actionButtonType);
        actionButton.setDisable(true);

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double val = Double.parseDouble(newVal);
                actionButton.setDisable(val <= 0 || (type.equals("Withdraw") && val > account.getBalance()));
            } catch (NumberFormatException e) {
                actionButton.setDisable(true);
            }
        });

        dialog.setResultConverter(btn -> btn == actionButtonType ?
                Double.parseDouble(amountField.getText()) : null);

        dialog.showAndWait().ifPresent(amount -> {
            String description = descriptionField.getText().isEmpty() ?
                    "Customer self-service " + type.toLowerCase() : descriptionField.getText();
            try {
                if (type.equals("Deposit")) {
                    bankingService.deposit(account.getAccountNumber(), amount, description);
                    refreshDashboard();
                    showAlert("Deposit Successful",
                            String.format("Deposited P%.2f to account #%d\nNew Balance: P%.2f",
                                    amount, account.getAccountNumber(),
                                    bankingService.getAccountBalance(account.getAccountNumber())));
                } else {
                    bankingService.withdraw(account.getAccountNumber(), amount, description);
                    refreshDashboard();
                    showAlert("Withdrawal Successful",
                            String.format("Withdrew P%.2f from account #%d\nNew Balance: P%.2f",
                                    amount, account.getAccountNumber(),
                                    bankingService.getAccountBalance(account.getAccountNumber())));
                }
            } catch (Exception e) {
                showAlert("Error", type + " failed: " + e.getMessage());
            }
        });
    }

    @FXML private void handleDeposit() {
        showTransactionDialog(accountsListView.getSelectionModel().getSelectedItem(), "Deposit");
    }

    @FXML private void handleWithdraw() {
        showTransactionDialog(accountsListView.getSelectionModel().getSelectedItem(), "Withdraw");
    }

    @FXML
    private void handleTransfer() {
        try {
            Account from = transferFromCombo.getValue();
            if (from == null) {
                showAlert("Error", "Select source account");
                return;
            }

            String toAccNumber = targetAccountField.getText().trim();
            if (toAccNumber.isEmpty()) {
                showAlert("Error", "Invalid target account");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(transferAmountField.getText());
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount");
                return;
            }

            if (amount <= 0) {
                showAlert("Error", "Amount must be positive");
                return;
            }

            if (from.getBalance() < amount) {
                showAlert("Error", "Insufficient funds");
                return;
            }

            Account to = bankingService.getAccount(toAccNumber);
            if (to == null) {
                showAlert("Error", "Target account not found");
                return;
            }

            String desc = transferDescription.getText().isEmpty() ?
                    "Transfer to account #" + toAccNumber :
                    transferDescription.getText();

            // Perform the transfer
            bankingService.withdraw(from.getAccountNumber(), amount, desc);
            bankingService.deposit(toAccNumber, amount, "Transfer from account #" + from.getAccountNumber());

            refreshDashboard();

            showAlert("Success", String.format(
                    "Transferred P%.2f from #%s to #%s\nNew Balance (Source): P%.2f",
                    amount,
                    from.getAccountNumber(),
                    toAccNumber,
                    bankingService.getAccountBalance(from.getAccountNumber())
            ));

            // Clear fields
            targetAccountField.clear();
            transferAmountField.clear();
            transferDescription.clear();

        } catch (Exception e) {
            showAlert("Error", "Transfer failed: " + e.getMessage());
        }
    }

    @FXML private void handleRefresh() { refreshDashboard(); }

    @FXML private void handleLogout() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Logout");
            confirm.setHeaderText("Are you sure you want to logout?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainmenu.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Banking System - Main Menu");
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(false);
            stage.show();

            ((Stage) welcomeLabel.getScene().getWindow()).close();

        } catch (Exception e) {
            showAlert("Error", "Logout failed: " + e.getMessage());
        }
    }

    @FXML private void handleViewProfile(){
    com.example.bankaccount.Customer loggedInCustomer = null;

        if (loggedInCustomer == null) {
            showAlert("Error", "No customer is logged in.");
            return;
        }
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("customerProfile.fxml"));
        Parent root = loader.load();

        com.example.bankaccount.CustomerProfileController controller = loader.getController();
        controller.setCustomer(loggedInCustomer);

        Stage stage = new Stage();
        stage.setTitle("Profile - " + loggedInCustomer.getDisplayName());
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();

    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Error", "Failed to open profile: " + e.getMessage());
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
