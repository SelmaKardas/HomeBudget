package com.example.selma.homebudget;

public class User {
    private String userEmail;
    private String userName;
    private String tokenId;

    public User(){}

    public User(String userEmail, String userName, String tokenId) {
        this.userEmail = userEmail;
        this.userName=userName;
        this.tokenId=tokenId;
    }

    public String getuserName() {
        return userName;
    }
    public String getTokenId() {
        return tokenId;
    }
    public String getuserEmail() {
        return userEmail;
    }

}
