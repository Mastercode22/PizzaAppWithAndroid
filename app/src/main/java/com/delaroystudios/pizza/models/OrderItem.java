package com.delaroystudios.pizza.models;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private int itemId;
    private int orderId;
    private int pizzaId;
    private int sizeId;
    private int crustId;
    private int quantity;
    private double itemPrice;
    private String specialInstructions;

    // Additional display fields
    private String pizzaName;
    private String sizeName;
    private String crustName;

    // Constructors
    public OrderItem() {
        this.quantity = 1;
    }

    public OrderItem(int orderId, int pizzaId, int sizeId, int crustId, int quantity, double itemPrice) {
        this.orderId = orderId;
        this.pizzaId = pizzaId;
        this.sizeId = sizeId;
        this.crustId = crustId;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
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

    // Utility methods
    public double getTotalPrice() {
        return itemPrice * quantity;
    }

    public String getFormattedPrice() {
        return String.format("GHS %.2f", itemPrice);
    }

    public String getFormattedTotalPrice() {
        return String.format("GHS %.2f", getTotalPrice());
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "itemId=" + itemId +
                ", pizzaName='" + pizzaName + '\'' +
                ", quantity=" + quantity +
                ", itemPrice=" + itemPrice +
                '}';
    }
}