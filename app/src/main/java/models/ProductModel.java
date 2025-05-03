package models;

import java.io.Serializable;

public class ProductModel implements Serializable {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String category;
    private double price;
    private double rating;
    private boolean popular;
    private boolean newProduct;

    public ProductModel() {
        // Empty constructor needed for Firebase
    }

    public ProductModel(String id, String name, String description, String imageUrl,
                        String category, double price, double rating,
                        boolean popular, boolean newProduct) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.popular = popular;
        this.newProduct = newProduct;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public boolean isNewProduct() {
        return newProduct;
    }

    public void setNewProduct(boolean newProduct) {
        this.newProduct = newProduct;
    }
}