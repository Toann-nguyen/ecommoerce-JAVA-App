package com.example.ecommerce;


import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adapters.ProductAdapter;
import adapters.SliderAdapter;
import models.Banner;
import models.Category;
import models.Product;
import models.ProductModel;
import repository.FirebaseRepository;


public class HomeMainActivity extends AppCompatActivity implements ProductAdapter.ProductClickListener {
    private static final String TAG = "HomeMainActivity";
    private RecyclerView productRecyclerView;
    private FirebaseAuth auth;
    private FirebaseRepository repository;
    private Handler handler;
    private Runnable bannerRunnable;

    // Views
    private MaterialToolbar topAppBar;
    private ChipGroup chipGroupCategories;
    private RecyclerView rvFlashSale;
    private RecyclerView rvFeatured;
    private ViewPager2 viewPagerSlider;
    private TextView tvCountdown;
    private LinearLayout dotsIndicator;

    // Adapters
    private ProductAdapter flashSaleAdapter;
    private ProductAdapter featuredAdapter;
    private SliderAdapter sliderAdapter; // Bạn cần tạo adapter này

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<Product> flashSaleProducts = new ArrayList<>();
    private List<Product> featuredProducts = new ArrayList<>();
    private List<Banner> banners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();
        repository = new FirebaseRepository();
        handler = new Handler(Looper.getMainLooper());


        initViews();
        setupTopAppBar();
        setupViewPager();
        setupRecyclerViews();
        repository.getAllProducts(new FirebaseRepository.OnProductsLoadListener() {
            @Override
            public void onProductsLoaded(List<ProductModel> products) {
                Log.d(TAG, "Loaded " + products.size() + " products successfully");
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading products: " + errorMessage);
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải sản phẩm: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        loadData();
        setupCountdownTimer();

    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        rvFlashSale = findViewById(R.id.rvFlashSale);
        rvFeatured = findViewById(R.id.rvFeatured);
        rvFeatured.setLayoutManager(new LinearLayoutManager(this));
        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        tvCountdown = findViewById(R.id.tvCountdown);




        // Tạo layout cho dots indicator nếu cần
        dotsIndicator = new LinearLayout(this);
        dotsIndicator.setOrientation(LinearLayout.HORIZONTAL);
        dotsIndicator.setId(View.generateViewId());

    }

    private void setupTopAppBar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        topAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_cart) {
                // Mở trang giỏ hàng
                Toast.makeText(this, "Cart action clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_search) {
                // Mở trang tìm kiếm
                Toast.makeText(this, "Search action clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_logout) {
                // Đăng xuất
                auth.signOut();
                Intent intent = new Intent(HomeMainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupViewPager() {
        // Tạo adapter cho ViewPager2 (bạn cần tạo adapter này)
        sliderAdapter = new SliderAdapter(this, banners);
        viewPagerSlider.setAdapter(sliderAdapter);

        // Thiết lập auto scroll cho ViewPager2
        viewPagerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBannerIndicator(position);
            }
        });
    }

    private void setupRecyclerViews() {
        // Flash Sale RecyclerView
        flashSaleAdapter = new ProductAdapter(this, flashSaleProducts, this);
        rvFlashSale.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFlashSale.setAdapter(flashSaleAdapter);

        // Featured Products RecyclerView
        featuredAdapter = new ProductAdapter(this, featuredProducts, this);
        rvFeatured.setLayoutManager(new GridLayoutManager(this, 2));
        rvFeatured.setAdapter(featuredAdapter);
    }

    private void loadData() {
        // Load ALL products into 'Featured' section
        repository.getAllProducts(new FirebaseRepository.OnProductsLoadListener() {
            @Override
            public void onProductsLoaded(List<ProductModel> models) {

                featuredProducts.clear();
                for (ProductModel m : models) {
                    featuredProducts.add(new Product());
                }
                featuredAdapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(HomeMainActivity.this,
                        "Error loading all products: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Load Flash Sale Products
        repository.getFlashSaleProducts(new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> list) {
                flashSaleProducts.clear();
                flashSaleProducts.addAll(list);
                flashSaleAdapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(HomeMainActivity.this,
                        "Error loading flash sale: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Load Category Chips
        repository.getCategories(new FirebaseRepository.CategoriesCallback() {
            @Override
            public void onCallback(List<Category> list) {
                categories.clear(); categories.addAll(list);
                chipGroupCategories.removeAllViews();
                for (Category c : categories) {
                    Chip chip = new Chip(HomeMainActivity.this);
                    chip.setText(c.getName());
                    chipGroupCategories.addView(chip);
                }
            }
            @Override
            public void onError(String error) {}
        });

        // Load Categories
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
                flashSaleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải sản phẩm khuyến mãi: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Load Featured Products
        repository.getFeaturedProducts(new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> productList) {
                featuredProducts.clear();
                featuredProducts.addAll(productList);
                featuredAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải sản phẩm nổi bật: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Load Banners
        repository.getBanners(new FirebaseRepository.BannersCallback() {
            @Override
            public void onCallback(List<Banner> bannerList) {
                banners.clear();
                banners.addAll(bannerList);
                sliderAdapter.notifyDataSetChanged();
                setupBannerAutoScroll();
                if (!banners.isEmpty()) {
                    updateBannerIndicator(0);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeMainActivity.this,
                        "Lỗi tải banner: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryChips() {
        chipGroupCategories.removeAllViews();
        for (Category category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chip.setOnClickListener(v -> onCategoryClick(category));
            chipGroupCategories.addView(chip);
        }
    }

    private void updateBannerIndicator(int activePosition) {
        if (banners.size() <= 1) return;

        // Tạo custom layout cho dots indicator nếu chưa có
        if (dotsIndicator.getParent() == null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dotsIndicator.setLayoutParams(params);
            dotsIndicator.setGravity(android.view.Gravity.CENTER);

            // Thêm vào layout (bạn cần có một container trong XML để thêm vào)
            // Hoặc tạo dynamically và thêm vào parent của viewPagerSlider
            // Ví dụ:
            // ((ViewGroup) viewPagerSlider.getParent()).addView(dotsIndicator);
        }

        dotsIndicator.removeAllViews();

        // Kích thước và margin cho dots
        int dotSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics()
        );
        int dotMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics()
        );

        // Tạo dots cho mỗi banner
        for (int i = 0; i < banners.size(); i++) {
            View dot = new View(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);

            GradientDrawable dotDrawable = new GradientDrawable();
            dotDrawable.setShape(GradientDrawable.OVAL);

            if (i == activePosition) {
                dotDrawable.setColor(getResources().getColor(R.color.purple_500));
            } else {
                dotDrawable.setColor(Color.parseColor("#CCCCCC"));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dot.setBackground(dotDrawable);
            } else {
                dot.setBackgroundDrawable(dotDrawable);
            }

            dotsIndicator.addView(dot);
        }
    }

    private void setupBannerAutoScroll() {
        if (banners.size() <= 1) return;

        if (bannerRunnable != null) {
            handler.removeCallbacks(bannerRunnable);
        }

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (viewPagerSlider.getCurrentItem() + 1) % banners.size();
                viewPagerSlider.setCurrentItem(nextItem, true);
                handler.postDelayed(this, 3000);
            }
        };

        handler.postDelayed(bannerRunnable, 3000);
    }

    private void setupCountdownTimer() {
        // Thiết lập timer đếm ngược cho Flash Sale (trong thực tế, thời gian này nên được lấy từ server)
        new Timer().scheduleAtFixedRate(new TimerTask() {
            int hours = 5;
            int minutes = 32;
            int seconds = 45;

            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (seconds > 0) {
                        seconds--;
                    } else {
                        if (minutes > 0) {
                            minutes--;
                            seconds = 59;
                        } else {
                            if (hours > 0) {
                                hours--;
                                minutes = 59;
                                seconds = 59;
                            } else {
                                // Khi hết thời gian Flash Sale
                                tvCountdown.setText("00 : 00 : 00");
                                cancel();
                                return;
                            }
                        }
                    }

                    String formattedTime = String.format("%02d : %02d : %02d", hours, minutes, seconds);
                    tvCountdown.setText(formattedTime);
                });
            }
        }, 0, 1000);
    }

    // ProductClickListener Implementation
    @Override
    public void onProductClick(Product product) {
        // Chuyển đến màn hình chi tiết sản phẩm
        Toast.makeText(this, "Đã chọn sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, ProductDetailActivity.class);
        // intent.putExtra("PRODUCT_ID", product.getId());
        // startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        // Thêm sản phẩm vào giỏ hàng
        Toast.makeText(this, product.getName() + " đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    // Category Click Handler
    public void onCategoryClick(Category category) {
        // Chuyển đến màn hình sản phẩm theo danh mục
        Toast.makeText(this, "Đã chọn danh mục: " + category.getName(), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, CategoryProductsActivity.class);
        // intent.putExtra("CATEGORY_ID", category.getId());
        // intent.putExtra("CATEGORY_NAME", category.getName());
        // startActivity(intent);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && bannerRunnable != null) {
            handler.removeCallbacks(bannerRunnable);
        }
    }
}