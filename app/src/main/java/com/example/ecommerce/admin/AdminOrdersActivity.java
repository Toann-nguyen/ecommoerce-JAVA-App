package com.example.ecommerce.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import adapters.AdminOrderAdapter;
import models.Order;
import models.User;
import repository.AdminFirebaseRepository;
import utils.PermissionManager;

public class AdminOrdersActivity extends AppCompatActivity {
    private static final String TAG = "AdminOrdersActivity";

    private MaterialToolbar toolbar;
    private SearchView searchView;
    private TabLayout tabLayout;
    private RecyclerView ordersRecyclerView;
    private TextView txtEmptyState;
    private ProgressBar progressBar;

    private List<Order> orderList = new ArrayList<>();
    private List<Order> filteredOrderList = new ArrayList<>();
    private AdminOrderAdapter adapter;
    private AdminFirebaseRepository repository;
    private PermissionManager permissionManager;

    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        // Khởi tạo Permission Manager
        permissionManager = PermissionManager.getInstance();

        // Khởi tạo Firebase Repository
        repository = new AdminFirebaseRepository();

        // Khởi tạo UI components
        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();

        // Kiểm tra quyền admin
        permissionManager.loadCurrentUser(new PermissionManager.UserLoadCallback() {
            @Override
            public void onUserLoaded(models.User user) {
                if (!permissionManager.isAdmin()) {
                    Toast.makeText(AdminOrdersActivity.this, "Bạn không có quyền truy cập khu vực này", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                // Load danh sách đơn hàng
                loadOrders();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminOrdersActivity.this, "Không thể tải thông tin người dùng: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        tabLayout = findViewById(R.id.tabLayout);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        progressBar = findViewById(R.id.progressBar);

        // Setup search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterOrders(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterOrders(newText);
                return false;
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý đơn hàng");
        }
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "all";
                        break;
                    case 1:
                        currentFilter = Order.STATUS_PENDING;
                        break;
                    case 2:
                        currentFilter = Order.STATUS_CONFIRMED;
                        break;
                    case 3:
                        currentFilter = Order.STATUS_SHIPPING;
                        break;
                    case 4:
                        currentFilter = Order.STATUS_DELIVERED;
                        break;
                    case 5:
                        currentFilter = Order.STATUS_CANCELLED;
                        break;
                }
                applyFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(this, filteredOrderList, new AdminOrderAdapter.OrderItemClickListener() {
            @Override
            public void onOrderClick(Order order) {
                navigateToOrderDetail(order);
            }
        });
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);
    }

    private void loadOrders() {
        showProgressBar(true);
        repository.getAllOrders(new AdminFirebaseRepository.OrdersCallback() {
            @Override
            public void onCallback(List<Order> orders) {
                showProgressBar(false);
                orderList.clear();
                if (orders != null && !orders.isEmpty()) {
                    orderList.addAll(orders);
                    applyFilters(); // Apply current filter and search
                } else {
                    showEmptyState(true, "Không có đơn hàng nào");
                }
            }

            @Override
            public void onError(String errorMessage) {
                showProgressBar(false);
                showEmptyState(true, "Lỗi: " + errorMessage);
                Toast.makeText(AdminOrdersActivity.this,
                        "Lỗi tải đơn hàng: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrders(String query) {
        if (TextUtils.isEmpty(query)) {
            // Reset search filter but maintain status filter
            applyFilters();
            return;
        }

        query = query.toLowerCase().trim();
        List<Order> searchResults = new ArrayList<>();

        for (Order order : orderList) {
            if ((currentFilter.equals("all") || order.getStatus().equals(currentFilter)) &&
                    (matchesQuery(order, query))) {
                searchResults.add(order);
            }
        }

        updateRecyclerView(searchResults);
    }

    private boolean matchesQuery(Order order, String query) {
        if (order == null || query == null) return false;

        try {
            // Search by order ID
            if (order.getId() != null && order.getId().toLowerCase().contains(query)) {
                return true;
            }

            // Search by customer name or email
            String userName = order.getUserName();
            if (userName != null && !userName.trim().isEmpty() && userName.toLowerCase().contains(query)) {
                return true;
            }

            String userEmail = order.getUserEmail();
            if (userEmail != null && !userEmail.trim().isEmpty() && userEmail.toLowerCase().contains(query)) {
                return true;
            }
        } catch (Exception e) {
            // Ignore search errors and return false
            return false;
        }

        return false;
    }

    private void applyFilters() {
        if (orderList.isEmpty()) {
            showEmptyState(true, "Không có đơn hàng nào");
            return;
        }

        List<Order> result = new ArrayList<>();
        String searchQuery = searchView.getQuery().toString().toLowerCase().trim();

        for (Order order : orderList) {
            try {
                String orderStatus = order.getStatus();
                if (orderStatus == null) {
                    orderStatus = Order.STATUS_PENDING; // Default to pending if null
                }

                if (currentFilter.equals("all") || orderStatus.equals(currentFilter)) {
                    if (TextUtils.isEmpty(searchQuery) || matchesQuery(order, searchQuery)) {
                        result.add(order);
                    }
                }
            } catch (Exception e) {
                // Skip problematic order
                continue;
            }
        }

        updateRecyclerView(result);
    }

    private void updateRecyclerView(List<Order> orders) {
        filteredOrderList.clear();
        filteredOrderList.addAll(orders);
        adapter.notifyDataSetChanged();

        showEmptyState(orders.isEmpty(), "Không tìm thấy đơn hàng nào");
    }

    private void showEmptyState(boolean show, String message) {
        if (show) {
            txtEmptyState.setText(message);
            txtEmptyState.setVisibility(View.VISIBLE);
            ordersRecyclerView.setVisibility(View.GONE);
        } else {
            txtEmptyState.setVisibility(View.GONE);
            ordersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void navigateToOrderDetail(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("ORDER_ID", order.getId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload orders when returning to this activity
        loadOrders();
    }
}