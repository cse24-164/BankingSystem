package com.example.bankaccount;

import java.util.Date;

public interface InterestBearing {
    boolean applyInterestIfDue();
    Date getLastInterestDate();
    void setLastInterestDate(Date lastInterestDate);
}