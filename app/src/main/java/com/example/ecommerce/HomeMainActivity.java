package com.example.ecommerce;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ecommerce.admin.AdminPanelActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adapters.FlashSaleBannerAdapter;
import adapters.ProductAdapter;
import adapters.SliderAdapter;
import models.Category;
import models.Product;
import models.ProductModel;
import models.ShoppingCart;
import repository.FirebaseRepository;
import utils.PermissionManager;

public class HomeMainActivity extends AppCompatActivity implements ProductAdapter.ProductClickListener {
    private static final String TAG = "HomeMainActivity";
    private RecyclerView productRecyclerView;
    private FirebaseAuth auth;
    private FirebaseRepository repository;
    private Handler handler;
    private Runnable bannerRunnable;
    private PermissionManager permissionManager;

    // Views
    private MaterialToolbar topAppBar;
    private ChipGroup chipGroupCategories;
    private RecyclerView rvFeatured;
    private RecyclerView rvAllProducts;
    private ViewPager2 viewPagerFlashSaleBanner;
    private TextView tvCountdown;
    private LinearLayout dotsIndicator;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView tvSearchStatus;

    // Adapters
    private ProductAdapter featuredAdapter;
    private ProductAdapter allProductsAdapter;
    private FlashSaleBannerAdapter flashSaleBannerAdapter;

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<Product> featuredProducts = new ArrayList<>();
    private List<Product> flashSaleProducts = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredFeaturedProducts = new ArrayList<>();
    private List<Product> filteredAllProducts = new ArrayList<>();
    private String selectedCategory = null;
    private Handler flashSaleBannerHandler;
    private Runnable flashSaleBannerRunnable;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main_drawer);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();
        repository = new FirebaseRepository();
        permissionManager = PermissionManager.getInstance();
        flashSaleBannerHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupTopAppBar();
        setupRecyclerViews();
        setupNavigationDrawer();
        setupFlashSaleBannerAutoScroll();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        rvAllProducts     = findViewById(R.id.rvAllProducts);
        rvFeatured = findViewById(R.id.rvFeatured);
        rvFeatured.setLayoutManager(new LinearLayoutManager(this));
        tvCountdown = findViewById(R.id.tvCountdown);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        viewPagerFlashSaleBanner = findViewById(R.id.viewPagerFlashSaleBanner);
        tvSearchStatus = findViewById(R.id.tvSearchStatus);

        // Thiết lập adapter cho Flash Sale Banner
        if (viewPagerFlashSaleBanner != null) {
            flashSaleBannerAdapter = new FlashSaleBannerAdapter(this, flashSaleProducts, product -> {
                onProductClick(product);
            });
            viewPagerFlashSaleBanner.setAdapter(flashSaleBannerAdapter);
            viewPagerFlashSaleBanner.setOffscreenPageLimit(1);
        }

        // Tạo layout cho dots indicator nếu cần
        dotsIndicator = new LinearLayout(this);
        dotsIndicator.setOrientation(LinearLayout.HORIZONTAL);
        dotsIndicator.setId(View.generateViewId());
    }

    private void setupTopAppBar() {
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        topAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_cart) {
                Intent intent = new Intent(HomeMainActivity.this, CartActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_logout) {
                auth.signOut();
                Intent intent = new Intent(HomeMainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // Setup search functionality
        MenuItem searchItem = topAppBar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchView);
    }

    private void setupRecyclerViews() {
        featuredAdapter = new ProductAdapter(this, filteredFeaturedProducts, this, ProductAdapter.TYPE_FEATURED);
        rvFeatured.setLayoutManager(new GridLayoutManager(this, 2));
        rvFeatured.setAdapter(featuredAdapter);
        rvFeatured.setNestedScrollingEnabled(true);

        rvAllProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvAllProducts.setNestedScrollingEnabled(true);
        allProductsAdapter = new ProductAdapter(this, filteredAllProducts, this, ProductAdapter.TYPE_FEATURED);
        rvAllProducts.setAdapter(allProductsAdapter);
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already on home, do nothing
            } else if (id == R.id.nav_categories) {
                // Handle categories action
                Toast.makeText(this, "Categories clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(HomeMainActivity.this, CartActivity.class));
            } else if (id == R.id.nav_profile) {
                // Handle profile action
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_orders) {
                // Handle orders action
                Toast.makeText(this, "Orders clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_admin_products) {
                // Kiểm tra quyền trước khi chuyển hướng
                if (permissionManager.canAccessAdminArea()) {
                    startActivity(new Intent(HomeMainActivity.this, AdminPanelActivity.class));
                } else {
                    Toast.makeText(this, "Bạn không có quyền truy cập khu vực quản trị", Toast.LENGTH_SHORT).show();
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Hiển thị hoặc ẩn menu Admin dựa vào quyền hạn
        updateNavigationMenu();
    }

    /**
     * Hiển thị hoặc ẩn các mục menu dựa theo quyền của người dùng
     */
    private void updateNavigationMenu() {
        Menu navMenu = navigationView.getMenu();

        // Tìm menu item admin
        MenuItem adminMenuItem = navMenu.findItem(R.id.nav_admin_products);
        if (adminMenuItem != null) {
            // Hiển thị menu admin chỉ khi người dùng có quyền
            adminMenuItem.setVisible(permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Cập nhật thông tin người dùng và quyền truy cập
        permissionManager.loadCurrentUser();
        updateNavigationMenu();

        // Khôi phục auto scroll khi activity resume
        setupFlashSaleBannerAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Tạm dừng auto scroll khi activity bị pause
        if (flashSaleBannerHandler != null && flashSaleBannerRunnable != null) {
            flashSaleBannerHandler.removeCallbacks(flashSaleBannerRunnable);
        }
    }

    private void loadData() {
        repository.getCategories(new FirebaseRepository.CategoriesCallback() {
            @Override
            public void onCallback(List<Category> categoryList) {
                categories.clear();
                categories.addAll(categoryList);
                setupCategoryChips();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải danh mục: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Load Flash Sale Products
        repository.getFlashSaleProducts(new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> productList) {
                flashSaleProducts.clear();
                flashSaleProducts.addAll(productList);

                // Cập nhật adapter cho Flash Sale Banner
                if (flashSaleBannerAdapter != null) {
                    flashSaleBannerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải sản phẩm Flash Sale: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Load Featured Products
        repository.getFeaturedProducts(new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> productList) {
                featuredProducts.clear();
                featuredProducts.addAll(productList);
                filteredFeaturedProducts.clear();
                filteredFeaturedProducts.addAll(productList);
                featuredAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải sản phẩm nổi bật: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Load All Products
        repository.getAllProducts(new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> productList) {
                allProducts.clear();
                allProducts.addAll(productList);
                filteredAllProducts.clear();
                filteredAllProducts.addAll(productList);
                allProductsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải tất cả sản phẩm: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryChips() {
        chipGroupCategories.removeAllViews();

        // Thêm chip "Tất cả" (All)
        Chip allChip = new Chip(this);
        allChip.setText("Tất cả");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setId(View.generateViewId());
        chipGroupCategories.addView(allChip);

        // Thêm các chip danh mục
        for (Category category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chipGroupCategories.addView(chip);
        }

        // Thiết lập sự kiện khi chọn chip
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            // Tìm chip được chọn
            Chip selectedChip = findViewById(checkedId);
            if (selectedChip != null) {
                String categoryName = selectedChip.getText().toString();

                if (categoryName.equals("Tất cả")) {
                    selectedCategory = null;
                    updateProductsByCategory(null);

                    // Ẩn thông báo đã chọn
                    hideSelectedCategoryMessage();
                } else {
                    selectedCategory = categoryName;
                    updateProductsByCategory(categoryName);

                    // Hiển thị thông báo đã chọn danh mục
                    showSelectedCategoryMessage(categoryName);
                }
            }
        });
    }

    /**
     * Hiển thị thông báo đã chọn danh mục
     */
    private void showSelectedCategoryMessage(String categoryName) {
        // Tạo Snackbar để hiển thị thông báo đã chọn danh mục
        View rootView = findViewById(android.R.id.content);
        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(rootView,
                "Đã chọn danh mục: " + categoryName, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);

        // Thêm màu nền tím và text màu trắng
        snackbar.setBackgroundTint(getResources().getColor(R.color.purple_700));
        snackbar.setTextColor(Color.WHITE);

        // Hiển thị Snackbar
        snackbar.show();
    }

    /**
     * Ẩn thông báo đã chọn danh mục
     */
    private void hideSelectedCategoryMessage() {
        // Optional: có thể thêm code để ẩn thông báo nếu cần
    }

    /**
     * Cập nhật danh sách sản phẩm theo danh mục đã chọn
     */
    private void updateProductsByCategory(String categoryName) {
        if (categoryName == null) {
            // Hiển thị tất cả sản phẩm
            showAllProducts();
            // Reset search when changing category
            resetSearch();
            return;
        }

        // Lọc sản phẩm theo danh mục
        repository.getProductsByCategory(categoryName, new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> products) {
                // Cập nhật danh sách all products
                allProducts.clear();
                allProducts.addAll(products);
                filteredAllProducts.clear();
                filteredAllProducts.addAll(products);

                // Apply search filter if needed
                if (!TextUtils.isEmpty(currentSearchQuery)) {
                    performSearch(currentSearchQuery);
                } else {
                    // Cập nhật adapter
                    allProductsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải sản phẩm: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hiển thị lại tất cả sản phẩm
     */
    private void showAllProducts() {
        repository.getAllProducts(new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> products) {
                allProducts.clear();
                allProducts.addAll(products);
                filteredAllProducts.clear();
                filteredAllProducts.addAll(products);

                // Apply search filter if needed
                if (!TextUtils.isEmpty(currentSearchQuery)) {
                    performSearch(currentSearchQuery);
                } else {
                    allProductsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải sản phẩm: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFlashSaleBannerAutoScroll() {
        if (viewPagerFlashSaleBanner == null || flashSaleProducts.size() <= 1) return;

        if (flashSaleBannerHandler == null) {
            flashSaleBannerHandler = new Handler(Looper.getMainLooper());
        }

        if (flashSaleBannerRunnable != null) {
            flashSaleBannerHandler.removeCallbacks(flashSaleBannerRunnable);
        }

        flashSaleBannerRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPagerFlashSaleBanner.getCurrentItem();
                int nextItem = (currentItem + 1) % flashSaleProducts.size();
                viewPagerFlashSaleBanner.setCurrentItem(nextItem, true);

                // Tiếp tục tự động chuyển trang sau 3 giây
                flashSaleBannerHandler.postDelayed(this, 3000);
            }
        };

        // Bắt đầu tự động chuyển trang sau 3 giây
        flashSaleBannerHandler.postDelayed(flashSaleBannerRunnable, 3000);
    }

    @Override
    public void onProductClick(Product product) {
        Toast.makeText(this, "Đã chọn sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeMainActivity.this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        ShoppingCart shoppingCart = ShoppingCart.getInstance(this);
        shoppingCart.addItem(product, 1);
        Toast.makeText(this, product.getName() + " đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra nếu người dùng chưa đăng nhập, chuyển về MainActivity
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(HomeMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            // Nếu đã đăng nhập, có thể tải lại dữ liệu nếu cần
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Xóa callbacks khi destroy activity
        if (flashSaleBannerHandler != null && flashSaleBannerRunnable != null) {
            flashSaleBannerHandler.removeCallbacks(flashSaleBannerRunnable);
        }
    }

    /**
     * Set up the search view functionality
     */
    private void setupSearchView(SearchView searchView) {
        if (searchView != null) {
            // Set hints and behavior
            searchView.setQueryHint("Tìm kiếm sản phẩm...");
            searchView.setIconifiedByDefault(true);

            // Set listeners for search actions
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    performSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    performSearch(newText);
                    return true;
                }
            });

            // Set behavior on search close
            searchView.setOnCloseListener(() -> {
                resetSearch();
                return false;
            });
        }
    }

    /**
     * Perform search on all products and featured products
     */
    private void performSearch(String query) {
        currentSearchQuery = query.toLowerCase().trim();

        // Filter featured products
        filteredFeaturedProducts.clear();
        if (TextUtils.isEmpty(currentSearchQuery)) {
            filteredFeaturedProducts.addAll(featuredProducts);
        } else {
            for (Product product : featuredProducts) {
                if (productMatchesSearch(product, currentSearchQuery)) {
                    filteredFeaturedProducts.add(product);
                }
            }
        }

        // Filter all products
        filteredAllProducts.clear();
        if (TextUtils.isEmpty(currentSearchQuery)) {
            filteredAllProducts.addAll(allProducts);
        } else {
            for (Product product : allProducts) {
                if (productMatchesSearch(product, currentSearchQuery)) {
                    filteredAllProducts.add(product);
                }
            }
        }

        // Update adapters with filtered results
        updateAdaptersWithFilteredData();
    }

    /**
     * Check if product matches the search query
     */
    private boolean productMatchesSearch(Product product, String searchQuery) {
        // Check if product name, description, or category contains the search query
        String productName = product.getName().toLowerCase();
        String productDescription = product.getDescription() != null ?
                product.getDescription().toLowerCase() : "";
        String productCategory = product.getCategory() != null ?
                product.getCategory().toLowerCase() : "";

        return productName.contains(searchQuery) ||
                productDescription.contains(searchQuery) ||
                productCategory.contains(searchQuery);
    }

    /**
     * Reset search and show all products again
     */
    private void resetSearch() {
        currentSearchQuery = "";
        filteredFeaturedProducts.clear();
        filteredFeaturedProducts.addAll(featuredProducts);
        filteredAllProducts.clear();
        filteredAllProducts.addAll(allProducts);

        // Hide search status
        tvSearchStatus.setVisibility(View.GONE);

        updateAdaptersWithFilteredData();
    }

    /**
     * Update adapters with the filtered data
     */
    private void updateAdaptersWithFilteredData() {
        // Update featured products adapter
        if (featuredAdapter != null) {
            featuredAdapter = new ProductAdapter(HomeMainActivity.this,
                    filteredFeaturedProducts, HomeMainActivity.this, ProductAdapter.TYPE_FEATURED);
            rvFeatured.setAdapter(featuredAdapter);
        }

        // Update all products adapter
        if (allProductsAdapter != null) {
            allProductsAdapter = new ProductAdapter(HomeMainActivity.this,
                    filteredAllProducts, HomeMainActivity.this, ProductAdapter.TYPE_FEATURED);
            rvAllProducts.setAdapter(allProductsAdapter);
        }

        // Update search status UI
        updateSearchStatusUI();
    }

    /**
     * Update the search status UI based on current search state
     */
    private void updateSearchStatusUI() {
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            // Show search status with results count
            int totalResults = filteredFeaturedProducts.size() + filteredAllProducts.size();
            String statusText = "Đang hiển thị " + totalResults + " kết quả tìm kiếm cho '" + currentSearchQuery + "'";
            tvSearchStatus.setText(statusText);
            tvSearchStatus.setVisibility(View.VISIBLE);

            // Show a message if no products were found
            if (filteredFeaturedProducts.isEmpty() && filteredAllProducts.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy sản phẩm nào phù hợp", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Hide search status when not searching
            tvSearchStatus.setVisibility(View.GONE);
        }
    }
}