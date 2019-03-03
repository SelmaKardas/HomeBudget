package com.example.selma.homebudget;

public class Category {
    private String categoryName;
    private String categoryId;

    public Category(){}

    public Category(String categoryId, String categoryName) {
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public String getCategoryId() {
        return categoryId;
    }

}
