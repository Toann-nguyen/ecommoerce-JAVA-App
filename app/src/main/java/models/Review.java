package models;

import com.google.firebase.Timestamp;

public class Review {
    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private String productId;
    private String productName;
    private String productImage;
    private String comment;
    private float rating;
    private Timestamp createdAt;
    private String orderId;

    // Empty constructor for Firebase
    public Review() {
    }

    public Review(String userId, String userEmail, String userName, String productId, String productName, String productImage, String comment, float rating, String orderId) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.comment = comment;
        this.rating = rating;
        this.createdAt = Timestamp.now();
        this.orderId = orderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}