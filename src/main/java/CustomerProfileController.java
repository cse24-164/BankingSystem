package com.example.bankaccount;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.SimpleDateFormat;

public class CustomerProfileController {

    @FXML private Label customerIdLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;

    // Individual-specific
    @FXML private Label idNumberLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;

    // Company-specific
    @FXML private Label regNumberLabel;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public void setCustomer(Customer customer) {
        customerIdLabel.setText(String.valueOf(customer.getCustomerId()));
        emailLabel.setText(customer.getEmail());
        phoneLabel.setText(customer.getPhoneNumber());
        addressLabel.setText(customer.getAddress());

        if (customer instanceof Individual ind) {
            nameLabel.setText(ind.getFirstName() + " " + ind.getSurname());
            idNumberLabel.setText(ind.getIdNumber());
            dobLabel.setText(dateFormat.format(ind.getDateOfBirth()));
            genderLabel.setText(ind.getGender());

            // Hide company-specific
            regNumberLabel.setText("N/A");

        } else if (customer instanceof Company comp) {
            nameLabel.setText(comp.getCompanyName());
            regNumberLabel.setText(comp.getRegistrationNumber());

            // Hide individual-specific
            idNumberLabel.setText("N/A");
            dobLabel.setText("N/A");
            genderLabel.setText("N/A");
        }
    }
}
