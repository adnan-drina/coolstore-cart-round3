package com.demo.model;

import java.io.Serializable;

public class ShoppingCartItem implements Serializable {

	private static final long serialVersionUID = -1234567890123456789L;
	private String productId;
	private int quantity;
	private double unitPrice;
	
	public ShoppingCartItem() {
		
	}
	
	public ShoppingCartItem(String productId, int quantity, double unitPrice) {
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}
	
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
	
	public double getUnitPrice() {
		return unitPrice;
	}
	
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}
	
	public double getTotalPrice() {
		return quantity * unitPrice;
	}
	
	@Override
	public String toString() {
		return "ShoppingCartItem [productId=" + productId + ", quantity=" 
				+ quantity + ", unitPrice=" + unitPrice + "]";
	}
	
}