package com.example.bankaccount;
import java.util.Date;

public class Individual extends Customer {
    private String idNumber;
    private Date dateOfBirth;
    private String emailAddress;
    private EmploymentInfo employmentInfo;

    public Individual(String firstName, String surname, String address, String pin,
                      String idNumber, Date dateOfBirth, String emailAddress) {
        super(firstName, surname, address, pin);
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
        this.emailAddress = emailAddress;
        this.employmentInfo = null;
    }

    public Individual(String firstName, String surname, String address, String pin,
                      String idNumber, Date dateOfBirth, String emailAddress, EmploymentInfo employmentInfo) {
        super(firstName, surname, address, pin);
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
        this.emailAddress = emailAddress;
        this.employmentInfo = employmentInfo;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setEmploymentInfo(EmploymentInfo employmentInfo) {
        this.employmentInfo = employmentInfo;
    }

    public EmploymentInfo getEmploymentInfo() {
        return employmentInfo;
    }

    public boolean isEmployed(){
        return employmentInfo != null;
    }
}