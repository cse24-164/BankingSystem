package com.example.bankaccount;

import java.util.Date;

public class Individual extends com.example.bankaccount.Customer {
    private String firstName;
    private String surname;
    private String idNumber;
    private Date dateOfBirth;
    private String gender;

    //Next of Kin
    private String nextOfKinName;
    private String nextOfKinRelationship;
    private String nextOfKinGender;
    private String nextOfKinPhoneNumber;

    //Source of Income
    private String sourceOfIncome;
    private String sourceName;
    private String sourceAddress;
    private double monthlyIncome;


    public Individual(String firstName, String surname, String address, String idNumber,
                      Date dateOfBirth, String gender, String email, String phoneNumber,
                      // Next of Kin
                      String nextOfKinName, String nextOfKinRelationship,
                      String nextOfKinGender, String nextOfKinPhoneNumber,
                      // Source of Income
                      String sourceOfIncome, String sourceName, String sourceAddress, double monthlyIncome) {

        super(address, email, phoneNumber, "", "");

        this.firstName = firstName;
        this.surname = surname;
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;

        this.nextOfKinName = nextOfKinName;
        this.nextOfKinRelationship = nextOfKinRelationship;
        this.nextOfKinGender = nextOfKinGender;
        this.nextOfKinPhoneNumber = nextOfKinPhoneNumber;

        this.sourceOfIncome = sourceOfIncome;
        this.sourceName = sourceName;
        this.sourceAddress = sourceAddress;
        this.monthlyIncome = monthlyIncome;
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getIdNumber() { return idNumber; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }

    public String getNextOfKinName() { return nextOfKinName; }
    public String getNextOfKinRelationship() { return nextOfKinRelationship; }
    public String getNextOfKinGender() { return nextOfKinGender; }
    public String getNextOfKinPhoneNumber() { return nextOfKinPhoneNumber; }

    public String getSourceOfIncome() { return sourceOfIncome; }
    public String getSourceName() { return sourceName; }
    public String getEmployerName() { return sourceName; }
    public String getEmployerAddress() { return sourceAddress; }
    public double getMonthlyIncome() { return monthlyIncome; }

    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setGender(String gender) { this.gender = gender; }

    public void setNextOfKinName(String nextOfKinName) { this.nextOfKinName = nextOfKinName; }
    public void setNextOfKinRelationship(String nextOfKinRelationship) { this.nextOfKinRelationship = nextOfKinRelationship; }
    public void setNextOfKinPhoneNumber(String nextOfKinPhoneNumber) {
        this.nextOfKinPhoneNumber = nextOfKinPhoneNumber;
    }

    public void setSourceOfIncome(String sourceOfIncome) { this.sourceOfIncome = sourceOfIncome;}
    public void setEmployerName(String employerName) {this.sourceName = sourceName;}
    public void setEmployerAddress(String employerAddress) {this.sourceAddress = sourceAddress;}
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome;}

    @Override
    public String getDisplayName() {
        return firstName + " " + surname;
    }

    @Override
    public String getIdentificationNumber() {
        return idNumber;
    }

    @Override
    public boolean isValidCustomer() {
        boolean basicInfoValid = firstName != null && !firstName.trim().isEmpty() &&
                surname != null && !surname.trim().isEmpty() &&
                idNumber != null && !idNumber.trim().isEmpty() &&
                getAddress() != null && !getAddress().trim().isEmpty();

        boolean nextOfKinValid = nextOfKinName != null && !nextOfKinName.trim().isEmpty() &&
                nextOfKinRelationship != null && !nextOfKinRelationship.trim().isEmpty() &&
                nextOfKinPhoneNumber != null && !nextOfKinPhoneNumber.trim().isEmpty();

        return basicInfoValid && nextOfKinValid;
    }


    public String getFullName() {
        return getDisplayName();
    }

    public boolean isEmployed() {
        return "Employment".equalsIgnoreCase(sourceOfIncome)
                && sourceName != null && !sourceName.trim().isEmpty()
                && sourceAddress != null && !sourceAddress.trim().isEmpty();
    }

    public boolean hasVerifiedIncome() {
        return this.sourceOfIncome != null && !this.sourceOfIncome.isEmpty() && this.monthlyIncome > 0;
    }


}
