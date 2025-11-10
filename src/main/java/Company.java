package com.example.bankaccount;

import java.util.Date;

public class Company extends com.example.bankaccount.Customer {
    private String companyName;
    private String registrationNumber;
    private String businessType;
    private String contactPersonName;
    private String sourceOfIncome;
    private double annualRevenue;

    public Company(String username, String password,
                   String address, String email, String phoneNumber,
                   String companyName, String registrationNumber, String businessType,
                   String contactPersonName, String sourceOfIncome, double annualRevenue) {
        super(address, email, phoneNumber, null, null);

        this.companyName = companyName;
        this.registrationNumber = registrationNumber;
        this.businessType = businessType;
        this.contactPersonName = contactPersonName;
        this.sourceOfIncome = sourceOfIncome;
        this.annualRevenue = annualRevenue;
    }

    //Getters
    public String getCompanyName() { return companyName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getBusinessType() { return businessType; }
    public String getContactPersonName() { return contactPersonName; }
    public String getSourceOfIncome() { return sourceOfIncome; }
    public double getAnnualRevenue() { return annualRevenue; }

    //Setters
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public void setContactPersonName(String contactPersonName) { this.contactPersonName = contactPersonName;}
    public void setSourceOfIncome(String sourceOfIncome) { this.sourceOfIncome = sourceOfIncome; }
    public void setAnnualRevenue(double annualRevenue) {this.annualRevenue = annualRevenue; }


    @Override
    public String getDisplayName() {
        return companyName;
    }

    @Override
    public String getIdentificationNumber() {
        return registrationNumber;
    }

    @Override
    public boolean isValidCustomer() {
        return companyName != null && !companyName.trim().isEmpty() &&
                registrationNumber != null && !registrationNumber.trim().isEmpty() &&
                address != null && !address.trim().isEmpty() &&
                contactPersonName != null && !contactPersonName.trim().isEmpty();
    }
}