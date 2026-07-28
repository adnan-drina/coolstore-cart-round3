package com.redhat.coolstore.model;

import java.io.Serializable;

public class ShoppingCartItem implements Serializable {
    private String productId;
    private int quantity;
    private double price;
    
    public ShoppingCartItem() {}
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
}