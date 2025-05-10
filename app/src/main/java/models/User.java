package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mô hình dữ liệu người dùng hệ thống
 */
public class User implements Serializable {
    private String uid;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String avatarUrl;
    private String role;
    private List<String> permissions;

    // Constructor mặc định cần thiết cho Firebase
    public User() {
        this.permissions = new ArrayList<>();
    }

    // Constructor với các tham số cơ bản
    public User(String uid, String email, String fullName) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = UserRole.USER.getRole(); // Default role
        this.permissions = new ArrayList<>();
    }

    // Constructor đầy đủ
    public User(String uid, String email, String fullName, String phone, String address, String avatarUrl, String role) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role : UserRole.USER.getRole();
        this.permissions = new ArrayList<>();
    }

    // Getters và Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * Thêm quyền cho người dùng
     *
     * @param permission Quyền cần thêm
     */
    public void addPermission(String permission) {
        if (!this.permissions.contains(permission)) {
            this.permissions.add(permission);
        }
    }

    /**
     * Kiểm tra người dùng có quyền được chỉ định không
     *
     * @param permission Quyền cần kiểm tra
     * @return true nếu có quyền, false nếu không
     */
    public boolean hasPermission(String permission) {
        return this.permissions.contains(permission);
    }

    /**
     * Kiểm tra người dùng có phải là Admin không
     *
     * @return true nếu là admin, false nếu không
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.getRole().equals(this.role);
    }

    /**
     * Kiểm tra người dùng có phải là Seller không
     *
     * @return true nếu là seller, false nếu không
     */
    public boolean isSeller() {
        return UserRole.SELLER.getRole().equals(this.role);
    }
}