package com.example.bakebliss_bakery.models;

public class OrderModel {
    private String orderId; // Firestore document ID
    private String foodName;
    private String status; // "Completed" or "Pending"
    private double price;
    private int quantity;
    private String orderDate;

    public OrderModel() {
    }

    public OrderModel(String orderId, String foodName, String status, double price, int quantity, String orderDate) {
        this.orderId = orderId;
        this.foodName = foodName;
        this.status = status;
        this.price = price;
        this.quantity = quantity;
        this.orderDate = orderDate;
    }

    public OrderModel(String foodName, String status, double price, int quantity, String orderDate) {
        this("", foodName, status, price, quantity, orderDate);
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
}