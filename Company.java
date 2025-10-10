package com.example.bankaccount;

public class Company extends Customer {
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String companySignatory;

    public Company(String companyName, String companyAddress, String companyPhone,
                   String companyEmail, String companySignatory) {
        super(companyName, "", companyAddress, "company123");
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.companySignatory = companySignatory;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanySignatory() {
        return companySignatory;
    }

    public void setCompanySignatory(String companySignatory) {
        this.companySignatory = companySignatory;
    }
}