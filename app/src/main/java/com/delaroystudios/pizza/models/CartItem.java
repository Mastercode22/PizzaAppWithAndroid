package com.delaroystudios.pizza.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private int cartId;
    private int userId;
    private int pizzaId;
    private int sizeId;
    private int crustId;
    private int quantity;
    private String addedAt;

    // Additional display fields
    private String pizzaName;
    private String sizeName;
    private String crustName;
    private double basePrice;

    // Constructors
    public CartItem() {
        this.quantity = 1;
    }

    public CartItem(int userId, int pizzaId, int sizeId, int crustId, int quantity) {
        this.userId = userId;
        this.pizzaId = pizzaId;
        this.sizeId = sizeId;
        this.crustId = crustId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPizzaId() {
        return pizzaId;
    }

    public void setPizzaId(int pizzaId) {
        this.pizzaId = pizzaId;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public int getCrustId() {
        return crustId;
    }

    public void setCrustId(int crustId) {
        this.crustId = crustId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }

    public String getPizzaName() {
        return pizzaName;
    }

    public void setPizzaName(String pizzaName) {
        this.pizzaName = pizzaName;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getCrustName() {
        return crustName;
    }

    public void setCrustName(String crustName) {
        this.crustName = crustName;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    // Utility methods
    public double getTotalPrice() {
        return basePrice * quantity;
    }

    public String getFormattedPrice() {
        return String.format("GHS %.2f", basePrice);
    }

    public String getFormattedTotalPrice() {
        return String.format("GHS %.2f", getTotalPrice());
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "cartId=" + cartId +
                ", pizzaName='" + pizzaName + '\'' +
                ", quantity=" + quantity +
                ", basePrice=" + basePrice +
                '}';
    }
}