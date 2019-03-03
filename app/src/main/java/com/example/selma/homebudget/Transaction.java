package com.example.selma.homebudget;

import java.util.Date;

public class Transaction {
    private String email, userName, transactionId, transactionName;
    private float amount;
    Boolean isIncome;
    private Category category;
    private Date date;

    public Transaction(){}




    public Transaction(String email, String userName, String transactionName, String transactionId, float amount, Boolean isIncome, Category category, Date date) {
        this.userName=userName;
        this.email=email;
        this.transactionName=transactionName;
        this.transactionId=transactionId;
        this.amount = amount;
        this.isIncome = isIncome;
        this.category = category;
        this.date=date;

    }
    public String getTransactionName() {
        return transactionName;
    }
    public String getEmail() {
        return email;
    }
    public String getTransactionId() {
        return transactionId;
    }
    public String getUserName() {
        return userName;
    }
    public float getAmount() {
        return amount;
    }
    public Date getDate() {
        return date;
    }
    public Category getCategory() {
        return category;
    }

}
