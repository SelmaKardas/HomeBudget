package com.example.selma.homebudget;

public class Budget {
    private String budgetId, budgetName, userEmail;

    public Budget(){}

    public Budget(String budgetId, String budgetName, String userEmail) {
        this.budgetId = budgetId;
        this.budgetName = budgetName;
        this.userEmail = userEmail;
    }

    public String getBudgetId() {
        return budgetId;
    }
    //fhfhdf

    public String getBudgetName() {
        return budgetName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
