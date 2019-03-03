package com.example.selma.homebudget;

public class Product {
    private String productId, productName;
    Category category;
    private boolean izActiveProduct;

    public Product(){}

    public Product(String productId, String productName, Category category, boolean izActiveProduct) {
        this.productId = productId;
        this.productName = productName;
        this.category=category;
        this.izActiveProduct = izActiveProduct;

    }
    public Category getCategory() {
        return category;
    }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public boolean getizActiveProduct() {
        return izActiveProduct;
    }

}
