package com.example.bakebliss_bakery.models;

public class FoodModel {
    private int id;
    private String name;
    private String description;
    private double price;

    // Constructor to initialize the FoodModel object
    public FoodModel(int id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Getter methods to access the private variables outside this class
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    // --- Setter methods to dynamically update the name and price (Required for Discounts) ---
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}