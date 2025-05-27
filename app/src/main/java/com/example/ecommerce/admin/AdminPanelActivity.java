package com.example.ecommerce.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ecommerce.R;
import com.google.android.material.appbar.MaterialToolbar;

import models.User;
import utils.PermissionManager;

/**
 * Màn hình quản lý trung tâm cho admin
 */
public class AdminPanelActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private CardView cardProducts, cardOrders, cardUsers, cardReports, cardChat, cardReviews;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Khởi tạo Permission Manager
        permissionManager = PermissionManager.getInstance();

        // Khởi tạo UI
        initViews();
        setupToolbar();

        // Tải thông tin người dùng từ Firestore trước khi kiểm tra quyền
        permissionManager.loadCurrentUser(new PermissionManager.UserLoadCallback() {
            @Override
            public void onUserLoaded(User user) {
                // Kiểm tra quyền truy cập sau khi đã tải thông tin người dùng
                if (!permissionManager.canAccessAdminArea()) {
                    Toast.makeText(AdminPanelActivity.this, "Bạn không có quyền truy cập khu vực này", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Thiết lập các chức năng của trang admin
                    setupCardClickListeners();
                    updateUIBasedOnPermissions();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminPanelActivity.this, "Lỗi tải thông tin người dùng: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        cardProducts = findViewById(R.id.cardProducts);
        cardOrders = findViewById(R.id.cardOrders);
        cardUsers = findViewById(R.id.cardUsers);
        cardReports = findViewById(R.id.cardReports);
        cardChat = findViewById(R.id.cardChat);
        cardReviews = findViewById(R.id.cardReviews);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCardClickListeners() {
        // Quản lý sản phẩm
        cardProducts.setOnClickListener(v -> {
            if (permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS)) {
                startActivity(new Intent(this, AdminProductsActivity.class));
            } else {
                showPermissionDenied();
            }
        });

        // Quản lý đơn hàng
        cardOrders.setOnClickListener(v -> {
            if (permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_ORDERS)) {
                startActivity(new Intent(this, AdminOrdersActivity.class));
            } else {
                showPermissionDenied();
            }
        });

        // Quản lý người dùng
        cardUsers.setOnClickListener(v -> {
            if (permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_USERS)) {
                startActivity(new Intent(this, AdminUsersActivity.class));
            } else {
                showPermissionDenied();
            }
        });

        // Xem báo cáo
        cardReports.setOnClickListener(v -> {
            if (permissionManager.hasPermission(PermissionManager.PERMISSION_VIEW_REPORTS)) {
                // TODO: Navigate to Reports
                Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
            } else {
                showPermissionDenied();
            }
        });

        // Chat với người dùng
        cardChat.setOnClickListener(v -> {
            if (permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_USERS)) {
                startActivity(new Intent(this, AdminChatActivity.class));
            } else {
                showPermissionDenied();
            }
        });

        // Quản lý đánh giá
        cardReviews.setOnClickListener(v -> {
            if (permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS)) {
                startActivity(new Intent(this, AdminReviewsActivity.class));
            } else {
                showPermissionDenied();
            }
        });
    }

    /**
     * Hiển thị hoặc ẩn các tính năng dựa trên quyền của người dùng
     */
    private void updateUIBasedOnPermissions() {
        // Hiển thị quản lý sản phẩm chỉ khi có quyền
        cardProducts.setVisibility(
                permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS)
                        ? View.VISIBLE : View.GONE);

        // Hiển thị quản lý đơn hàng chỉ khi có quyền
        cardOrders.setVisibility(
                permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_ORDERS)
                        ? View.VISIBLE : View.GONE);

        // Hiển thị quản lý người dùng chỉ khi có quyền (chỉ admin)
        cardUsers.setVisibility(
                permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_USERS)
                        ? View.VISIBLE : View.GONE);

        // Hiển thị xem báo cáo chỉ khi có quyền
        cardReports.setVisibility(
                permissionManager.hasPermission(PermissionManager.PERMISSION_VIEW_REPORTS)
                        ? View.VISIBLE : View.GONE);

        // Hiển thị chat với người dùng chỉ khi có quyền
        cardChat.setVisibility(
                permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_USERS)
                        ? View.VISIBLE : View.GONE);

        // Hiển thị quản lý đánh giá chỉ khi có quyền quản lý sản phẩm
        cardReviews.setVisibility(
                permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS)
                        ? View.VISIBLE : View.GONE);
    }

    private void showPermissionDenied() {
        Toast.makeText(this, "Bạn không có quyền truy cập tính năng này", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật thông tin người dùng và quyền truy cập
        permissionManager.loadCurrentUser();
        updateUIBasedOnPermissions();
    }

    /**
     * Override back button to confirm exit
     */
    @Override
    public void onBackPressed() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận thoát")
                .setMessage("Bạn có muốn thoát khỏi khu vực quản trị?")
                .setPositiveButton("Có", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Không", null)
                .show();
    }
}
