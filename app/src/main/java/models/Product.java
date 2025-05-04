package models;

import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
    private String category;
    private boolean isFlashSale;
    private boolean isFeatured;
    private String description;
    private int stock;
    private int discount;
    private double rating;
    private boolean popular;
    private boolean newProduct;

    // Constructor mặc định cần thiết cho Firebase
    public Product() {}

    // Constructor đầy đủ tham số
    public Product(String id, String name, double price, String imageUrl, String category,
                   boolean isFlashSale, boolean isFeatured, String description, 
                   int stock, int discount, double rating, boolean popular, boolean newProduct) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isFlashSale = isFlashSale;
        this.isFeatured = isFeatured;
        this.description = description;
        this.stock = stock;
        this.discount = discount;
        this.rating = rating;
        this.popular = popular;
        this.newProduct = newProduct;
    }

    // Constructor để chuyển đổi từ ProductModel
    public Product(ProductModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.price = model.getPrice();
        this.imageUrl = model.getImageUrl();
        this.category = model.getCategory();
        this.description = model.getDescription();
        this.rating = model.getRating();
        this.popular = model.isPopular();
        this.newProduct = model.isNewProduct();
        // Các giá trị mặc định cho các trường mới
        this.stock = 0;
        this.discount = 0;
        this.isFlashSale = false;
        this.isFeatured = false;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isFlashSale() { return isFlashSale; }
    public void setFlashSale(boolean flashSale) { isFlashSale = flashSale; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }

    public boolean isNewProduct() { return newProduct; }
    public void setNewProduct(boolean newProduct) { this.newProduct = newProduct; }

    // Phương thức tính giá sau khuyến mãi
    public double getDiscountedPrice() {
        if (discount > 0) {
            return price - (price * discount / 100.0);
        }
        return price;
    }
}

