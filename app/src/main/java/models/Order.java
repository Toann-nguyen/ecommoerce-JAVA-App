package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private List<CartItem> items;
    private ShippingAddress shippingAddress;
    private String paymentMethod;
    private double subtotal;
    private double discount;
    private double shippingFee;
    private double total;
    private String status;
    private Date orderDate;
    private Date deliveryDate;
    // Thêm thông tin người dùng
    private String userEmail;
    private String userName;

    // Trạng thái đơn hàng
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_SHIPPING = "shipping";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_CANCELLED = "cancelled";

    // Constructor rỗng cho Firebase
    public Order() {
    }

    public Order(String userId, List<CartItem> items, ShippingAddress shippingAddress,
                 String paymentMethod, double subtotal, double discount, double shippingFee) {
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.subtotal = subtotal;
        this.discount = discount;
        this.shippingFee = shippingFee;
        this.total = subtotal - discount + shippingFee;
        this.status = STATUS_PENDING;
        this.orderDate = new Date();
    }

    // Constructor bổ sung cho đặt hàng 1 sản phẩm
    public Order(String userId, String userEmail, String userName, CartItem item, ShippingAddress shippingAddress,
                 String paymentMethod, double subtotal, double discount, double shippingFee) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.items = new ArrayList<>();
        this.items.add(item);
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.subtotal = subtotal;
        this.discount = discount;
        this.shippingFee = shippingFee;
        this.total = subtotal - discount + shippingFee;
        this.status = STATUS_PENDING;
        this.orderDate = new Date();
    }

    // Getters and setters
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

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    // Thêm getter và setter cho thông tin người dùng
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
}