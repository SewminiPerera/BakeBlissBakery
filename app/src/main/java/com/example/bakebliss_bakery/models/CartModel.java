package com.example.bakebliss_bakery.models;

public class CartModel {
    private int cartId;
    private String foodName;
    private double price;
    private int quantity;

    public CartModel(int cartId, String foodName, double price, int quantity) {
        this.cartId = cartId;
        this.foodName = foodName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getCartId() { return cartId; }
    public String getFoodName() { return foodName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}