package com.example.bakebliss_bakery.models;

public class FoodModel {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String documentId;
    private String imageUrl;
    private boolean isSuperDeal;
    private double discountPercent;

    public FoodModel() {
        this.documentId = "";
        this.imageUrl = "";
        this.isSuperDeal = false;
        this.discountPercent = 0.0;
    }

    public FoodModel(int id, String name, String description, double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.documentId = "";
        this.imageUrl = "";
        this.isSuperDeal = false;
        this.discountPercent = 0.0;
    }

    public FoodModel(int id, String name, String description, double price, String category, String documentId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.documentId = documentId;
        this.imageUrl = "";
        this.isSuperDeal = false;
        this.discountPercent = 0.0;
    }

    public FoodModel(int id, String name, String description, double price, String category, String documentId, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.documentId = documentId;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.isSuperDeal = false;
        this.discountPercent = 0.0;
    }

    public FoodModel(int id, String name, String description, double price, String category,
                     String documentId, String imageUrl, boolean isSuperDeal, double discountPercent) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.documentId = documentId;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.isSuperDeal = isSuperDeal;
        this.discountPercent = discountPercent;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl != null ? imageUrl : ""; }

    public boolean isSuperDeal() { return isSuperDeal; }
    public void setSuperDeal(boolean superDeal) { isSuperDeal = superDeal; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
}