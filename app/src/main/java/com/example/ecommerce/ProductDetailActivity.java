package com.example.ecommerce;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import adapters.ProductImageAdapter;
import models.Product;
import models.ShoppingCart;
import repository.FirebaseRepository;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    // UI components
    private MaterialToolbar topAppBar;
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutDots;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvDiscountPrice;
    private RatingBar ratingBar;
    private TextView tvRatingValue;
    private TextView tvStock;
    private TextView tvCategory;
    private TextView tvDescription;
    private TextView tvQuantity;
    private Button btnDecrease;
    private Button btnIncrease;
    private Button btnAddToCart;
    private Button btnBuyNow;

    // Data
    private Product product;
    private int quantity = 1;
    private List<String> productImages;
    private FirebaseFirestore db;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        repository = new FirebaseRepository();

        // Lấy dữ liệu sản phẩm từ Intent
        String productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo UI components
        initViews();
        setupToolbar();
        setupQuantityControls();
        setupButtons();

        // Tải thông tin sản phẩm từ Firestore
        loadProductDetails(productId);
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        layoutDots = findViewById(R.id.layoutDots);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvDiscountPrice = findViewById(R.id.tvDiscountPrice);
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        tvStock = findViewById(R.id.tvStock);
        tvCategory = findViewById(R.id.tvCategory);
        tvDescription = findViewById(R.id.tvDescription);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupQuantityControls() {
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (quantity < 99 && quantity < product.getStock()) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Số lượng tối đa có thể mua là " + product.getStock(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        btnAddToCart.setOnClickListener(v -> {
            if (product != null) {
                ShoppingCart shoppingCart = ShoppingCart.getInstance(this);
                shoppingCart.addItem(product, quantity);
                Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        btnBuyNow.setOnClickListener(v -> {
            if (product != null) {
                ShoppingCart shoppingCart = ShoppingCart.getInstance(this);
                shoppingCart.addItem(product, quantity);

                Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadProductDetails(String productId) {
        repository.getProductById(productId, new FirebaseRepository.ProductCallback() {
            @Override
            public void onCallback(Product loadedProduct) {
                if (loadedProduct != null) {
                    product = loadedProduct;
                    displayProductDetails();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lỗi khi tải sản phẩm: " + errorMessage);
                Toast.makeText(ProductDetailActivity.this, "Lỗi khi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProductDetails() {
        // Đặt tiêu đề cho toolbar
        topAppBar.setTitle(product.getName());

        // Hiển thị thông tin sản phẩm
        tvProductName.setText(product.getName());

        // Format giá tiền
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String price = currencyFormat.format(product.getPrice()) + " đ";
        tvProductPrice.setText(price);

        // Hiển thị giảm giá nếu có
        int discount = product.getDiscount();
        if (discount > 0) {
            tvDiscountPrice.setVisibility(View.VISIBLE);
            tvDiscountPrice.setText("Giảm " + discount + "%");
        } else {
            tvDiscountPrice.setVisibility(View.GONE);
        }

        // Hiển thị đánh giá
        float rating = (float) product.getRating();
        ratingBar.setRating(rating);
        tvRatingValue.setText(String.format("%.1f", rating));

        // Hiển thị tình trạng kho
        int stock = product.getStock();
        if (stock > 0) {
            tvStock.setText("Còn hàng (" + stock + ")");
            tvStock.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh lá
            btnAddToCart.setEnabled(true);
            btnBuyNow.setEnabled(true);
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(Color.parseColor("#F44336")); // Màu đỏ
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);
        }

        // Hiển thị danh mục
        tvCategory.setText(product.getCategory());

        // Hiển thị mô tả
        tvDescription.setText(product.getDescription());

        // Hiển thị slider ảnh sản phẩm
        setupProductImages();
    }

    private void setupProductImages() {
        // Trong trường hợp thực tế, sản phẩm có thể có nhiều hình ảnh
        // Ở đây, chúng ta chỉ có URL hình ảnh chính từ sản phẩm
        productImages = new ArrayList<>();

        // Thêm hình ảnh chính
        productImages.add(product.getImageUrl());

        // Trong thực tế, bạn có thể tải thêm hình ảnh từ Firestore
        // Tạm thời, thêm một vài hình ảnh giả định cho slider
        if (product.getCategory().equals("Điện thoại")) {
            productImages.add("https://firebasestorage.googleapis.com/v0/b/ecomapp-demo.appspot.com/o/phone_detail_1.jpg");
            productImages.add("https://firebasestorage.googleapis.com/v0/b/ecomapp-demo.appspot.com/o/phone_detail_2.jpg");
        } else if (product.getCategory().equals("Laptop")) {
            productImages.add("https://firebasestorage.googleapis.com/v0/b/ecomapp-demo.appspot.com/o/laptop_detail_1.jpg");
            productImages.add("https://firebasestorage.googleapis.com/v0/b/ecomapp-demo.appspot.com/o/laptop_detail_2.jpg");
        }

        // Thiết lập ViewPager2 với adapter
        ProductImageAdapter imageAdapter = new ProductImageAdapter(this, productImages);
        viewPagerImages.setAdapter(imageAdapter);

        // Thiết lập indicator dots
        setupImageIndicators(productImages.size());

        // Xử lý sự kiện khi chuyển trang
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });
    }

    private void setupImageIndicators(int count) {
        layoutDots.removeAllViews();
        if (count <= 1) {
            return; // Không cần hiển thị indicator nếu chỉ có 1 hình ảnh
        }

        // Tạo dots
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            int dotSize = 16; // Size của dot
            int dotMargin = 8; // Margin giữa các dots

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);

            // Tạo hình tròn với drawable
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);

            // Dot đầu tiên là active
            if (i == 0) {
                drawable.setColor(getResources().getColor(R.color.purple_500));
            } else {
                drawable.setColor(Color.LTGRAY);
            }

            // Thiết lập background cho dot
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dot.setBackground(drawable);
            } else {
                dot.setBackgroundDrawable(drawable);
            }

            // Thêm dot vào container
            layoutDots.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            View dot = layoutDots.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) dot.getBackground();

            if (i == position) {
                drawable.setColor(getResources().getColor(R.color.purple_500));
            } else {
                drawable.setColor(Color.LTGRAY);
            }
        }
    }
}