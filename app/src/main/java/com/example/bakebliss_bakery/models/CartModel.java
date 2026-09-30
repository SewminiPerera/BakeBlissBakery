package com.example.bakebliss_bakery.models;

public class CartModel {
    private String cartId;
    private String foodName;
    private double price;
    private int quantity;

    public CartModel() {
    }

    public CartModel(String cartId, String foodName, double price, int quantity) {
        this.cartId = cartId;
        this.foodName = foodName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}