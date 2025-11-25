package com.example.bankaccount;

import java.util.Date;

public interface InterestBearing {
    void applyInterestIfDue(Date customerRegistrationDate);
    Date getLastInterestDate();
    void setLastInterestDate(Date lastInterestDate);
}