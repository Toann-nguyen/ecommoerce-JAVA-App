package com.example.ecommerce.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import adapters.AdminProductAdapter;
import models.Product;
import repository.AdminFirebaseRepository;
import utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity implements AdminProductAdapter.OnProductItemClickListener {
    private static final String TAG = "AdminProductsActivity";

    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAddProduct;
    private MaterialToolbar topAppBar;
    private SearchView searchView;
    private ChipGroup sortChipGroup;
    private Chip chipSortName, chipSortNameDesc, chipSortPriceAsc, chipSortPriceDesc;

    private List<Product> productList;
    private List<Product> filteredList;
    private AdminProductAdapter adapter;
    private AdminFirebaseRepository repository;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền truy cập
        permissionManager = PermissionManager.getInstance();
        if (!permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS)) {
            Toast.makeText(this, "Bạn không có quyền truy cập trang này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_products);

        // Initialize Firebase
        repository = new AdminFirebaseRepository();

        // Initialize UI components
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        setupSearchView();
        setupSortingChips();

        // Load products with default sort (name, ascending)
        loadProducts("name", true, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload products when returning to this activity using current sort and search
        String currentSort = getCurrentSortField();
        boolean isAscending = isCurrentSortAscending();
        loadProducts(currentSort, isAscending, searchView.getQuery().toString());
    }

    private void initViews() {
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        topAppBar = findViewById(R.id.topAppBar);
        searchView = findViewById(R.id.searchView);
        sortChipGroup = findViewById(R.id.sortChipGroup);
        chipSortName = findViewById(R.id.chipSortName);
        chipSortNameDesc = findViewById(R.id.chipSortNameDesc);
        chipSortPriceAsc = findViewById(R.id.chipSortPriceAsc);
        chipSortPriceDesc = findViewById(R.id.chipSortPriceDesc);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminProductAdapter(this, filteredList, this);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddProduct.setOnClickListener(v -> {
            // Navigate to add product screen
            Intent intent = new Intent(AdminProductsActivity.this, EditProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadProducts(String sortBy, boolean ascending, String searchQuery) {
        showLoading(true);

        repository.getAllProducts(sortBy, ascending, searchQuery, new AdminFirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> products) {
                showLoading(false);

                if (products != null && !products.isEmpty()) {
                    productList.clear();
                    productList.addAll(products);
                    filterProducts(searchView.getQuery().toString());
                    showEmptyView(filteredList.isEmpty());
                } else {
                    showEmptyView(true);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showEmptyView(true);
                Toast.makeText(AdminProductsActivity.this,
                        "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading products: " + errorMessage);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String currentSort = getCurrentSortField();
                boolean isAscending = isCurrentSortAscending();
                loadProducts(currentSort, isAscending, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(product);
                }
            }
        }

        adapter.notifyDataSetChanged();
        showEmptyView(filteredList.isEmpty());
    }

    private void setupSortingChips() {
        // Mặc định sắp xếp theo tên tăng dần
        chipSortName.setChecked(true);

        chipSortName.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loadProducts("name", true, searchView.getQuery().toString());
            }
        });

        chipSortNameDesc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loadProducts("name", false, searchView.getQuery().toString());
            }
        });

        chipSortPriceAsc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loadProducts("price", true, searchView.getQuery().toString());
            }
        });

        chipSortPriceDesc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loadProducts("price", false, searchView.getQuery().toString());
            }
        });
    }

    private String getCurrentSortField() {
        if (chipSortPriceAsc.isChecked() || chipSortPriceDesc.isChecked()) {
            return "price";
        }
        return "name"; // Mặc định sắp xếp theo tên
    }

    private boolean isCurrentSortAscending() {
        return chipSortName.isChecked() || chipSortPriceAsc.isChecked();
    }

    @Override
    public void onEditClick(Product product) {
        // Navigate to edit product screen
        Intent intent = new Intent(AdminProductsActivity.this, EditProductActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Product product) {
        // Kiểm tra quyền xóa sản phẩm (chỉ admin mới được xóa)
        if (!permissionManager.isAdmin()) {
            Toast.makeText(this, "Chỉ quản trị viên mới có quyền xóa sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteProduct(Product product) {
        showLoading(true);

        repository.deleteProduct(product.getId(), new AdminFirebaseRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(AdminProductsActivity.this,
                        "Đã xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                loadProducts(getCurrentSortField(), isCurrentSortAscending(), searchView.getQuery().toString()); // Reload the list
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(AdminProductsActivity.this,
                        "Lỗi khi xóa: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error deleting product: " + errorMessage);
            }
        });
    }
}