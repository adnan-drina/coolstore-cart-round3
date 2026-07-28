package com.redhat.coolstore.model;

import java.io.Serializable;
import java.util.List;

public class ShoppingCart implements Serializable {
    private String cartId;
    private List<ShoppingCartItem> items;
    
    public ShoppingCart() {}
    
    public String getCartId() {
        return cartId;
    }
    
    public void setCartId(String cartId) {
        this.cartId = cartId;
    }
    
    public List<ShoppingCartItem> getItems() {
        return items;
    }
    
    public void setItems(List<ShoppingCartItem> items) {
        this.items = items;
    }
}