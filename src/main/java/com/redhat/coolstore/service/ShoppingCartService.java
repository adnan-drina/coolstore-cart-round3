package com.redhat.coolstore.service;

import com.redhat.coolstore.model.ShoppingCart;
import java.util.Optional;

public interface ShoppingCartService {
    Optional<ShoppingCart> getShoppingCart(String cartId);
    ShoppingCart addItem(String cartId, String itemId, int quantity) throws Exception;
    ShoppingCart set(String cartId, String tmpId) throws Exception;
    ShoppingCart deleteItem(String cartId, String itemId, int quantity) throws Exception;
    ShoppingCart checkout(String cartId);
}