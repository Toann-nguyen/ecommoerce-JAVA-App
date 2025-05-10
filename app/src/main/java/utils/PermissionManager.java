package utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.ecommerce.MainActivity;
import com.example.ecommerce.admin.AdminProductsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import models.User;
import models.UserRole;

/**
 * Quản lý phân quyền và kiểm tra quyền truy cập của người dùng
 */
public class PermissionManager {

    // Định nghĩa các permission
    public static final String PERMISSION_MANAGE_PRODUCTS = "manage_products";
    public static final String PERMISSION_MANAGE_ORDERS = "manage_orders";
    public static final String PERMISSION_MANAGE_USERS = "manage_users";
    public static final String PERMISSION_VIEW_REPORTS = "view_reports";

    private static PermissionManager instance;
    private User currentUser;
    private FirebaseFirestore db;
    private final String COLLECTION_USERS = "users";

    private PermissionManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Singleton pattern để lấy instance của PermissionManager
     */
    public static synchronized PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }

    /**
     * Tải thông tin người dùng hiện tại từ Firebase
     */
    public void loadCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection(COLLECTION_USERS).document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                        } else {
                            // Tạo user mới nếu không tồn tại
                            currentUser = new User(uid, firebaseUser.getEmail(), firebaseUser.getDisplayName());
                            // Lưu user mới vào Firestore
                            db.collection(COLLECTION_USERS).document(uid).set(currentUser);
                        }
                    })
                    .addOnFailureListener(e -> {
                        currentUser = null;
                    });
        } else {
            currentUser = null;
        }
    }

    /**
     * Tải thông tin người dùng hiện tại từ Firebase với callback
     * @param callback Interface để trả về kết quả sau khi tải xong
     */
    public void loadCurrentUser(UserLoadCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection(COLLECTION_USERS).document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                            callback.onUserLoaded(currentUser);
                        } else {
                            // Tạo user mới nếu không tồn tại
                            currentUser = new User(uid, firebaseUser.getEmail(), firebaseUser.getDisplayName());
                            // Lưu user mới vào Firestore
                            db.collection(COLLECTION_USERS).document(uid).set(currentUser)
                                    .addOnSuccessListener(aVoid -> callback.onUserLoaded(currentUser))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> {
                        currentUser = null;
                        callback.onError(e.getMessage());
                    });
        } else {
            currentUser = null;
            callback.onError("User not logged in");
        }
    }

    /**
     * Interface callback để nhận thông tin user sau khi tải xong
     */
    public interface UserLoadCallback {
        void onUserLoaded(User user);

        void onError(String error);
    }

    /**
     * Kiểm tra người dùng có phải là admin không
     */
    public boolean isAdmin() {
        try {
            return currentUser != null && currentUser.isAdmin();
        } catch (Exception e) {
            // Nếu có lỗi, giả định user không phải admin
            return false;
        }
    }

    /**
     * Kiểm tra người dùng có phải là seller không
     */
    public boolean isSeller() {
        return currentUser != null && currentUser.isSeller();
    }

    /**
     * Kiểm tra người dùng có quyền cụ thể không
     */
    public boolean hasPermission(String permission) {
        return currentUser != null && (currentUser.isAdmin() || currentUser.hasPermission(permission));
    }

    /**
     * Kiểm tra người dùng có thể truy cập admin area không
     */
    public boolean canAccessAdminArea() {
        return currentUser != null && (currentUser.isAdmin() || currentUser.isSeller());
    }

    /**
     * Kiểm tra quyền và hiển thị dialog nếu không có quyền
     *
     * @param context    Context hiện tại
     * @param permission Quyền cần kiểm tra
     * @return true nếu có quyền, false nếu không
     */
    public boolean checkPermissionWithDialog(Context context, String permission) {
        if (hasPermission(permission)) {
            return true;
        } else {
            showPermissionDeniedDialog(context);
            return false;
        }
    }

    /**
     * Hiển thị dialog thông báo không có quyền truy cập
     *
     * @param context Context hiện tại
     */
    private void showPermissionDeniedDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Không có quyền truy cập")
                .setMessage("Bạn không có quyền thực hiện hành động này.")
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Kiểm tra xem người dùng có thể truy cập quản lý sản phẩm không
     * và chuyển đến trang đó nếu có quyền
     *
     * @param context Context hiện tại
     */
    public void checkAndNavigateToProductManagement(Context context) {
        if (hasPermission(PERMISSION_MANAGE_PRODUCTS)) {
            context.startActivity(new Intent(context, AdminProductsActivity.class));
        } else {
            showPermissionDeniedDialog(context);
        }
    }

    /**
     * Khởi tạo quyền mặc định dựa theo vai trò người dùng
     *
     * @param user Người dùng cần khởi tạo quyền
     */
    public void initializeUserPermissions(User user) {
        if (user != null) {
            // Xóa tất cả quyền hiện tại
            user.getPermissions().clear();

            // Phân quyền theo vai trò
            if (user.isAdmin()) {
                // Admin có tất cả các quyền
                user.addPermission(PERMISSION_MANAGE_PRODUCTS);
                user.addPermission(PERMISSION_MANAGE_ORDERS);
                user.addPermission(PERMISSION_MANAGE_USERS);
                user.addPermission(PERMISSION_VIEW_REPORTS);
            } else if (user.isSeller()) {
                // Seller có quyền quản lý sản phẩm và xem báo cáo
                user.addPermission(PERMISSION_MANAGE_PRODUCTS);
                user.addPermission(PERMISSION_VIEW_REPORTS);
            }
            // User thông thường không có quyền đặc biệt nào

            // Cập nhật user trong Firestore
            if (user.getUid() != null) {
                db.collection(COLLECTION_USERS).document(user.getUid())
                        .set(user)
                        .addOnFailureListener(e -> {
                            // Xử lý lỗi nếu cần
                        });
            }
        }
    }

    /**
     * Đăng xuất và xóa thông tin người dùng hiện tại
     */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        currentUser = null;
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Cài đặt thông tin người dùng hiện tại
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Cập nhật thông tin người dùng vào Firestore sau khi thay đổi
     */
    public void updateCurrentUser() {
        if (currentUser != null && currentUser.getUid() != null) {
            db.collection(COLLECTION_USERS).document(currentUser.getUid())
                    .set(currentUser)
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi nếu cần
                    });
        }
    }
}