package com.example.bakebliss_bakery.models;

public class OrderModel {
    private String foodName;
    private String status; // "Completed" or "Pending"
    private double price;
    private int quantity;
    private String orderDate; // Added to store the date and time

    // Constructor to initialize the OrderModel with date
    public OrderModel(String foodName, String status, double price, int quantity, String orderDate) {
        this.foodName = foodName;
        this.status = status;
        this.price = price;
        this.quantity = quantity;
        this.orderDate = orderDate; // Assigning the date
    }

    // Getters
    public String getFoodName() { return foodName; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getOrderDate() { return orderDate; } // NEW: Getter for the date
}