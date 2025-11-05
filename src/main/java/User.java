package com.example.bankaccount;

public abstract class User {
    protected String username;
    protected String password;
    protected String userType;

    public User(String username, String password, String userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password;}
    public String getUserType() { return userType; }

    //Setters
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setUserType(String userType) { this.userType = userType; }
}
