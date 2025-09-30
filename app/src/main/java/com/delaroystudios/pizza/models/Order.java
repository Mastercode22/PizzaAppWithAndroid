package com.delaroystudios.pizza.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Order implements Serializable {
    private int orderId;
    private int userId;
    private String orderDate;
    private double totalAmount;
    private String status;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String specialInstructions;
    private String estimatedCompletion;

    // Additional fields for display
    private List<OrderItem> orderItems;
    private int itemCount;

    // Order status constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    // Constructors
    public Order() {
        this.status = STATUS_PENDING;
        this.orderItems = new ArrayList<>();
    }

    public Order(int userId, String customerName, double totalAmount) {
        this.userId = userId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = STATUS_PENDING;
        this.orderItems = new ArrayList<>();
    }

    public Order(int orderId, int userId, String customerName, double totalAmount, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderItems = new ArrayList<>();
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getEstimatedCompletion() {
        return estimatedCompletion;
    }

    public void setEstimatedCompletion(String estimatedCompletion) {
        this.estimatedCompletion = estimatedCompletion;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        this.itemCount = orderItems != null ? orderItems.size() : 0;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    // Utility methods
    public String getFormattedTotal() {
        return String.format("GHS %.2f", totalAmount);
    }

    public String getFormattedOrderDate() {
        if (orderDate == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(orderDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return orderDate;
        }
    }

    public String getStatusDisplayName() {
        switch (status.toLowerCase()) {
            case STATUS_PENDING:
                return "Pending";
            case STATUS_IN_PROGRESS:
                return "In Progress";
            case STATUS_COMPLETED:
                return "Completed";
            case STATUS_CANCELLED:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isInProgress() {
        return STATUS_IN_PROGRESS.equals(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public boolean canBeCancelled() {
        return isPending();
    }

    public boolean canBeModified() {
        return isPending();
    }

    public void addOrderItem(OrderItem item) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        orderItems.add(item);
        itemCount = orderItems.size();
    }

    public double calculateSubtotal() {
        if (orderItems == null) return totalAmount;

        double subtotal = 0.0;
        for (OrderItem item : orderItems) {
            subtotal += item.getItemPrice() * item.getQuantity();
        }
        return subtotal;
    }

    public double calculateTax() {
        return calculateSubtotal() * 0.1; // 10% tax
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderDate='" + orderDate + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId;
    }

    @Override
    public int hashCode() {
        return orderId;
    }
}
