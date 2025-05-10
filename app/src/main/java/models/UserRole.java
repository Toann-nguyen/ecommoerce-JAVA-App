package models;

/**
 * Enum định nghĩa các vai trò người dùng trong ứng dụng
 */
public enum UserRole {
    USER("user"),       // Người dùng thông thường
    SELLER("seller"),   // Người bán hàng
    ADMIN("admin");     // Quản trị viên

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    /**
     * Chuyển đổi từ chuỗi sang enum UserRole
     *
     * @param role String biểu diễn vai trò
     * @return UserRole tương ứng hoặc mặc định USER nếu không tìm thấy
     */
    public static UserRole fromString(String role) {
        if (role != null) {
            for (UserRole userRole : UserRole.values()) {
                if (role.equalsIgnoreCase(userRole.role)) {
                    return userRole;
                }
            }
        }
        return USER; // Default to USER role
    }
}