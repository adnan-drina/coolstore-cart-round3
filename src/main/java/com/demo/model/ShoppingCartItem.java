package com.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import java.io.Serializable;

@Entity
@Table(name = "shopping_cart_item")
public class ShoppingCartItem implements Serializable {
	
	private static final long serialVersionUID = 6964558044240061049L;
	@Id
	private Long id;
	private double price;
	private int quantity;
	private double promoSavings;
	@OneToOne
	@JoinColumn(name = "product_id")
	private Product product;
	
	public ShoppingCartItem() {
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getPromoSavings() {
		return promoSavings;
	}

	public void setPromoSavings(double promoSavings) {
		this.promoSavings = promoSavings;
	}

	@Override
	public String toString() {
		return "ShoppingCartItem [price=" + price + ", quantity=" + quantity
				+ ", promoSavings=" + promoSavings + ", product=" + product
				+ "]";
	}
		
	
}
