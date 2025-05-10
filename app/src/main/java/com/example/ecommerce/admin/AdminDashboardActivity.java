package com.example.ecommerce.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.MainActivity;
import com.example.ecommerce.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import adapters.AdminProductAdapter;
import models.Product;
import repository.AdminFirebaseRepository;
import utils.PermissionManager;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboardActivity";

    private MaterialToolbar toolbar;
    private androidx.appcompat.widget.SearchView searchView;
    private RecyclerView productRecyclerView;
    private Button btnCreateProduct;
    private Button btnManageOrders;
    private TextView txtEmptyState;

    private List<Product> productList = new ArrayList<>();
    private AdminProductAdapter adapter;
    private AdminFirebaseRepository repository;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Khởi tạo Permission Manager
        permissionManager = PermissionManager.getInstance();

        // Kiểm tra quyền admin
        if (!permissionManager.isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập khu vực này", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        // Khởi tạo Firebase Repository
        repository = new AdminFirebaseRepository();

        // Khởi tạo UI components
        initViews();
        setupToolbar();
        setupRecyclerView();

        // Load danh sách sản phẩm
        loadProducts();

        // Setup nút tạo sản phẩm mới
        btnCreateProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, EditProductActivity.class);
            startActivity(intent);
        });

        // Setup nút quản lý đơn hàng
        btnManageOrders.setOnClickListener(v -> {
            // Kiểm tra quyền trước khi chuyển đến màn hình quản lý đơn hàng
            permissionManager.loadCurrentUser(new PermissionManager.UserLoadCallback() {
                @Override
                public void onUserLoaded(models.User user) {
                    // Người dùng đã được tải, chuyển đến màn hình quản lý đơn hàng
                    Intent intent = new Intent(AdminDashboardActivity.this, AdminOrdersActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Không thể tải thông tin người dùng: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        productRecyclerView = findViewById(R.id.productRecyclerView);
        btnCreateProduct = findViewById(R.id.btnCreateProduct);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        txtEmptyState = findViewById(R.id.txtEmptyState);

        // Setup search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return false;
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản lý sản phẩm");
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(this, productList, new AdminProductAdapter.OnProductItemClickListener() {
            @Override
            public void onEditClick(Product product) {
                Intent intent = new Intent(AdminDashboardActivity.this, EditProductActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Product product) {
                deleteProduct(product);
            }
        });
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productRecyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        txtEmptyState.setVisibility(View.GONE);
        repository.getAllProducts(new AdminFirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> products) {
                productList.clear();
                if (products != null && !products.isEmpty()) {
                    productList.addAll(products);
                    adapter.notifyDataSetChanged();
                } else {
                    txtEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi tải sản phẩm: " + errorMessage, Toast.LENGTH_SHORT).show();
                txtEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void deleteProduct(Product product) {
        repository.deleteProduct(product.getId(), new AdminFirebaseRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminDashboardActivity.this,
                        "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                loadProducts(); // Reload the list
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi xóa sản phẩm: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        if (query.isEmpty()) {
            loadProducts();
            return;
        }

        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getCategory().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }

        adapter.updateList(filteredList);
        txtEmptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);

        // Add Orders Management menu item
        menu.add(Menu.NONE, R.id.action_orders, Menu.NONE, "Quản lý đơn hàng")
                .setIcon(R.drawable.ic_order)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_orders) {
            // Navigate to AdminOrdersActivity
            Intent intent = new Intent(this, AdminOrdersActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}