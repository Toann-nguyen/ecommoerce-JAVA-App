package com.example.ecommerce.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.MainActivity;
import com.example.ecommerce.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import adapters.UserAdapter;
import models.User;
import models.UserRole;
import repository.AdminFirebaseRepository;
import utils.PermissionManager;

public class AdminUsersActivity extends AppCompatActivity implements UserAdapter.OnUserItemClickListener {
    private static final String TAG = "AdminUsersActivity";

    private MaterialToolbar toolbar;
    private SearchView searchView;
    private ChipGroup roleFilterChipGroup;
    private Chip chipAllUsers, chipAdmins, chipSellers, chipUsers;
    private RecyclerView usersRecyclerView;
    private TextView txtHeader;
    private TextView txtEmptyState;
    private ProgressBar progressBar;

    private List<User> userList = new ArrayList<>();
    private List<User> filteredUserList = new ArrayList<>();
    private UserAdapter adapter;
    private AdminFirebaseRepository repository;
    private PermissionManager permissionManager;

    private String currentFilter = "all";
    private String currentSortBy = "fullName"; // Default sort by name
    private boolean sortAscending = true; // Default ascending

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền admin
        permissionManager = PermissionManager.getInstance();
        if (!permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_USERS)) {
            Toast.makeText(this, "Bạn không có quyền truy cập khu vực này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_users);

        // Khởi tạo Repository
        repository = new AdminFirebaseRepository();

        // Khởi tạo UI components
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupSearchView();

        // Load users
        loadUsers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        roleFilterChipGroup = findViewById(R.id.roleFilterChipGroup);
        chipAllUsers = findViewById(R.id.chipAllUsers);
        chipAdmins = findViewById(R.id.chipAdmins);
        chipSellers = findViewById(R.id.chipSellers);
        chipUsers = findViewById(R.id.chipUsers);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        txtHeader = findViewById(R.id.txtHeader);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý người dùng");
        }

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sort_name_asc) {
                currentSortBy = "fullName";
                sortAscending = true;
                applyFilters();
                return true;
            } else if (id == R.id.sort_name_desc) {
                currentSortBy = "fullName";
                sortAscending = false;
                applyFilters();
                return true;
            } else if (id == R.id.sort_email_asc) {
                currentSortBy = "email";
                sortAscending = true;
                applyFilters();
                return true;
            } else if (id == R.id.sort_role_asc) {
                currentSortBy = "role";
                sortAscending = true;
                applyFilters();
                return true;
            } else if (id == R.id.action_refresh) {
                loadUsers();
                return true;
            } else if (id == R.id.action_logout) {
                permissionManager.logout();
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this, filteredUserList, this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        roleFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAllUsers) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipAdmins) {
                currentFilter = UserRole.ADMIN.getRole();
            } else if (checkedId == R.id.chipSellers) {
                currentFilter = UserRole.SELLER.getRole();
            } else if (checkedId == R.id.chipUsers) {
                currentFilter = UserRole.USER.getRole();
            }
            applyFilters();
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void loadUsers() {
        showLoading(true);
        repository.getAllUsers(currentSortBy, sortAscending, new AdminFirebaseRepository.UsersCallback() {
            @Override
            public void onCallback(List<User> users) {
                showLoading(false);
                userList.clear();
                if (users != null && !users.isEmpty()) {
                    userList.addAll(users);
                    applyFilters();
                } else {
                    showEmptyState(true, "Không có người dùng nào");
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showEmptyState(true, "Lỗi: " + errorMessage);
                Toast.makeText(AdminUsersActivity.this,
                        "Lỗi tải người dùng: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        if (userList.isEmpty()) {
            showEmptyState(true, "Không có người dùng nào");
            return;
        }

        String searchQuery = searchView.getQuery().toString().toLowerCase().trim();
        List<User> result = new ArrayList<>();

        for (User user : userList) {
            // Apply role filter
            if (!currentFilter.equals("all") && !currentFilter.equals(user.getRole())) {
                continue;
            }

            // Apply search filter
            if (!TextUtils.isEmpty(searchQuery)) {
                boolean matchesQuery = false;

                // Search in full name
                if (user.getFullName() != null &&
                        user.getFullName().toLowerCase().contains(searchQuery)) {
                    matchesQuery = true;
                }

                // Search in email
                if (!matchesQuery && user.getEmail() != null &&
                        user.getEmail().toLowerCase().contains(searchQuery)) {
                    matchesQuery = true;
                }

                if (!matchesQuery) {
                    continue; // Skip if doesn't match search
                }
            }

            result.add(user);
        }

        // Update header
        updateHeaderText(result.size());

        // Update adapter
        filteredUserList.clear();
        filteredUserList.addAll(result);
        adapter.notifyDataSetChanged();

        showEmptyState(result.isEmpty(), "Không tìm thấy người dùng nào phù hợp");
    }

    private void updateHeaderText(int count) {
        String roleText = "tất cả";
        switch (currentFilter) {
            case "admin":
                roleText = "admin";
                break;
            case "seller":
                roleText = "người bán";
                break;
            case "user":
                roleText = "thường";
                break;
        }

        txtHeader.setText("Người dùng " + roleText + " (" + count + ")");
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            usersRecyclerView.setVisibility(View.GONE);
            txtEmptyState.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            txtEmptyState.setText(message);
            txtEmptyState.setVisibility(View.VISIBLE);
            usersRecyclerView.setVisibility(View.GONE);
        } else {
            txtEmptyState.setVisibility(View.GONE);
            usersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUserClick(User user) {
        // Show user details dialog
        new AlertDialog.Builder(this)
                .setTitle("Thông tin người dùng")
                .setMessage("ID: " + user.getUid() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Họ tên: " + user.getFullName() + "\n" +
                        "Vai trò: " + getRoleDisplayName(user.getRole()) + "\n" +
                        "Số điện thoại: " + (user.getPhone() != null ? user.getPhone() : "Chưa cập nhật") + "\n" +
                        "Địa chỉ: " + (user.getAddress() != null ? user.getAddress() : "Chưa cập nhật"))
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public void onEditRoleClick(User user) {
        showEditRoleDialog(user);
    }

    private void showEditRoleDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user_role, null);

        // Find views
        TextView txtUserInfo = dialogView.findViewById(R.id.txtUserInfo);
        RadioGroup radioGroupRole = dialogView.findViewById(R.id.radioGroupRole);
        RadioButton radioAdmin = dialogView.findViewById(R.id.radioAdmin);
        RadioButton radioSeller = dialogView.findViewById(R.id.radioSeller);
        RadioButton radioUser = dialogView.findViewById(R.id.radioUser);
        CheckBox checkManageProducts = dialogView.findViewById(R.id.checkManageProducts);
        CheckBox checkManageOrders = dialogView.findViewById(R.id.checkManageOrders);
        CheckBox checkManageUsers = dialogView.findViewById(R.id.checkManageUsers);
        CheckBox checkViewReports = dialogView.findViewById(R.id.checkViewReports);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Set current user info
        txtUserInfo.setText(user.getFullName() + " (" + user.getEmail() + ")");

        // Set current role
        if (UserRole.ADMIN.getRole().equals(user.getRole())) {
            radioAdmin.setChecked(true);
        } else if (UserRole.SELLER.getRole().equals(user.getRole())) {
            radioSeller.setChecked(true);
        } else {
            radioUser.setChecked(true);
        }

        // Set current permissions
        checkManageProducts.setChecked(user.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS));
        checkManageOrders.setChecked(user.hasPermission(PermissionManager.PERMISSION_MANAGE_ORDERS));
        checkManageUsers.setChecked(user.hasPermission(PermissionManager.PERMISSION_MANAGE_USERS));
        checkViewReports.setChecked(user.hasPermission(PermissionManager.PERMISSION_VIEW_REPORTS));

        // Set role change listener
        radioGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAdmin) {
                // Admin has all permissions
                checkManageProducts.setChecked(true);
                checkManageOrders.setChecked(true);
                checkManageUsers.setChecked(true);
                checkViewReports.setChecked(true);

                // Disable checkboxes for admin
                checkManageProducts.setEnabled(false);
                checkManageOrders.setEnabled(false);
                checkManageUsers.setEnabled(false);
                checkViewReports.setEnabled(false);
            } else if (checkedId == R.id.radioSeller) {
                // Seller has some permissions by default
                checkManageProducts.setChecked(true);
                checkViewReports.setChecked(true);
                checkManageOrders.setChecked(false);
                checkManageUsers.setChecked(false);

                // Enable checkboxes for custom permissions
                checkManageProducts.setEnabled(true);
                checkManageOrders.setEnabled(true);
                checkManageUsers.setEnabled(true);
                checkViewReports.setEnabled(true);
            } else {
                // Regular user has no permissions by default
                checkManageProducts.setChecked(false);
                checkManageOrders.setChecked(false);
                checkManageUsers.setChecked(false);
                checkViewReports.setChecked(false);

                // Enable checkboxes for custom permissions
                checkManageProducts.setEnabled(true);
                checkManageOrders.setEnabled(true);
                checkManageUsers.setEnabled(true);
                checkViewReports.setEnabled(true);
            }
        });

        // Create dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Set button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Get selected role
            String selectedRole;
            if (radioAdmin.isChecked()) {
                selectedRole = UserRole.ADMIN.getRole();
            } else if (radioSeller.isChecked()) {
                selectedRole = UserRole.SELLER.getRole();
            } else {
                selectedRole = UserRole.USER.getRole();
            }

            // Update user object
            user.setRole(selectedRole);

            // Update permissions
            List<String> permissions = new ArrayList<>();
            if (checkManageProducts.isChecked()) {
                permissions.add(PermissionManager.PERMISSION_MANAGE_PRODUCTS);
            }
            if (checkManageOrders.isChecked()) {
                permissions.add(PermissionManager.PERMISSION_MANAGE_ORDERS);
            }
            if (checkManageUsers.isChecked()) {
                permissions.add(PermissionManager.PERMISSION_MANAGE_USERS);
            }
            if (checkViewReports.isChecked()) {
                permissions.add(PermissionManager.PERMISSION_VIEW_REPORTS);
            }
            user.setPermissions(permissions);

            // Save changes to Firebase
            updateUser(user);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateUser(User user) {
        showLoading(true);
        repository.updateUser(user, new AdminFirebaseRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(AdminUsersActivity.this,
                        "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
                loadUsers(); // Reload users
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(AdminUsersActivity.this,
                        "Lỗi cập nhật: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRoleDisplayName(String role) {
        if (UserRole.ADMIN.getRole().equals(role)) {
            return "Admin";
        } else if (UserRole.SELLER.getRole().equals(role)) {
            return "Người bán";
        } else {
            return "Người dùng";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_users_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data in case external changes were made
        loadUsers();
    }
}