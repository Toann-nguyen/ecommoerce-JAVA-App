package com.example.ecommerce;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import adapters.OrderAdapter;
import models.Order;

public class OrderHistoryActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private RecyclerView rvOrders;
    private TextView tvEmptyOrders;
    private TextView txtHeader;
    private SwipeRefreshLayout swipeRefresh;
    private OrderAdapter orderAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ChipGroup chipGroupStatus;
    private ChipGroup chipGroupSort;
    private SearchView searchView;

    private List<Order> orderList = new ArrayList<>();
    private List<Order> filteredOrderList = new ArrayList<>();
    private String currentFilter = "all";
    private String currentSortBy = "date";
    private boolean sortAscending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFilterChips();
        setupSortChips();
        setupSearch();
        loadOrders();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        rvOrders = findViewById(R.id.rvOrders);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        txtHeader = findViewById(R.id.txtHeader);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        chipGroupSort = findViewById(R.id.chipGroupSort);
        searchView = findViewById(R.id.searchView);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, filteredOrderList);
        // Fix the bug by setting item click listener manually
        orderAdapter.setItemClickListener(order -> navigateToOrderDetail(order));
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadOrders);
        swipeRefresh.setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.accent
        );
    }

    private void setupFilterChips() {
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) return;
            Chip chip = group.findViewById(checkedId);
            currentFilter = (String) chip.getTag();
            applyFilters();
        });
    }

    private void setupSortChips() {
        chipGroupSort.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) return;
            Chip chip = group.findViewById(checkedId);
            String sortTag = (String) chip.getTag();
            if (sortTag.equals(currentSortBy)) {
                sortAscending = !sortAscending;
            } else {
                currentSortBy = sortTag;
                sortAscending = true;
            }
            applyFilters();
            updateSortChipText(chip);
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void loadOrders() {
        if (!swipeRefresh.isRefreshing()) {
            showEmptyState(true, "Đang tải đơn hàng...");
        }

        if (currentUser == null) {
            swipeRefresh.setRefreshing(false);
            showEmptyState(true, "Vui lòng đăng nhập để xem lịch sử đơn hàng");
            return;
        }

        db.collection("orders")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    swipeRefresh.setRefreshing(false);
                    orderList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        if (order != null) {
                            order.setId(document.getId());
                            orderList.add(order);
                        } else {
                            // Log lỗi hoặc xử lý khi không thể chuyển đổi document thành Order
                            Log.e("OrderHistory", "Không thể chuyển đổi document " + document.getId() + " thành Order");
                        }
                    }

                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    showEmptyState(true, "Không thể tải lịch sử đơn hàng. Vui lòng thử lại sau.");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        if (orderList.isEmpty()) {
            showEmptyState(true, "Bạn chưa có đơn hàng nào");
            return;
        }

        List<Order> result = new ArrayList<>();
        String searchQuery = searchView.getQuery().toString().toLowerCase(Locale.ROOT).trim();

        for (Order order : orderList) {
            String orderStatus = order.getStatus();
            if (orderStatus == null) {
                orderStatus = Order.STATUS_PENDING;
            }

            if (currentFilter.equals("all") || orderStatus.equals(currentFilter)) {
                if (TextUtils.isEmpty(searchQuery) || matchesSearchQuery(order, searchQuery)) {
                    result.add(order);
                }
            }
        }

        // Sort results
        sortOrders(result);

        // Update UI
        updateRecyclerView(result);
        updateHeaderText(result.size());
    }

    private boolean matchesSearchQuery(Order order, String query) {
        return order.getId().toLowerCase(Locale.ROOT).contains(query) ||
                (order.getUserName() != null && order.getUserName().toLowerCase(Locale.ROOT).contains(query)) ||
                (order.getUserEmail() != null && order.getUserEmail().toLowerCase(Locale.ROOT).contains(query));
    }

    private void sortOrders(List<Order> orders) {
        switch (currentSortBy) {
            case "date":
                orders.sort((o1, o2) -> {
                    if (o1.getOrderDate() == null || o2.getOrderDate() == null) {
                        return 0;
                    }
                    int result = o1.getOrderDate().compareTo(o2.getOrderDate());
                    return sortAscending ? result : -result;
                });
                break;
            case "price":
                orders.sort((o1, o2) -> {
                    double result = o1.getTotal() - o2.getTotal();
                    return sortAscending ? (int) result : (int) -result;
                });
                break;
        }
    }

    private void updateHeaderText(int count) {
        String statusText = "tất cả";
        switch (currentFilter) {
            case Order.STATUS_PENDING:
                statusText = "chờ xác nhận";
                break;
            case Order.STATUS_CONFIRMED:
                statusText = "đã xác nhận";
                break;
            case Order.STATUS_SHIPPING:
                statusText = "đang giao";
                break;
            case Order.STATUS_DELIVERED:
                statusText = "đã giao";
                break;
            case Order.STATUS_CANCELLED:
                statusText = "đã hủy";
                break;
        }
        txtHeader.setText("Đơn hàng " + statusText + " (" + count + ")");
    }

    private void updateSortChipText(Chip chip) {
        String baseText = chip.getText().toString().replaceAll(" ↑|↓", "");
        chip.setText(baseText + (sortAscending ? " ↑" : " ↓"));
    }

    private void updateRecyclerView(List<Order> orders) {
        filteredOrderList.clear();
        filteredOrderList.addAll(orders);
        orderAdapter.notifyDataSetChanged();

        showEmptyState(orders.isEmpty(), "Không tìm thấy đơn hàng nào");
    }

    private void showEmptyState(boolean show, String message) {
        if (show) {
            tvEmptyOrders.setText(message);
            tvEmptyOrders.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToOrderDetail(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("ORDER_ID", order.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload orders when returning to this activity to show updated status
        loadOrders();
    }
}
