package com.example.ecommerce;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import adapters.CartAdapter;
import models.CartItem;
import models.Product;
import models.ShoppingCart;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartAdapterListener {

    private RecyclerView rvCartItems;
    private TextView tvEmptyCart;
    private TextView tvSubtotalValue;
    private TextView tvDiscountValue;
    private TextView tvTotalValue;
    private Button btnCheckout;
    private MaterialToolbar topAppBar;

    private ShoppingCart shoppingCart;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupToolbar();
        setupRecyclerView();

        // Lấy dữ liệu giỏ hàng và hiển thị
        shoppingCart = ShoppingCart.getInstance(this);
        updateCartUI();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotalValue = findViewById(R.id.tvSubtotalValue);
        tvDiscountValue = findViewById(R.id.tvDiscountValue);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        btnCheckout = findViewById(R.id.btnCheckout);
        topAppBar = findViewById(R.id.topAppBar);

        btnCheckout.setOnClickListener(v -> processCheckout());
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, shoppingCart != null ? shoppingCart.getCartItems() : null, this);
        rvCartItems.setAdapter(cartAdapter);
    }

    private void updateCartUI() {
        List<CartItem> items = shoppingCart.getCartItems();

        // Cập nhật adapter
        cartAdapter.updateCartItems(items);

        // Hiển thị thông báo nếu giỏ hàng trống
        if (items.isEmpty()) {
            rvCartItems.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(false);
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            tvEmptyCart.setVisibility(View.GONE);
            btnCheckout.setEnabled(true);
        }

        // Cập nhật thông tin tổng tiền
        updatePriceSummary();
    }

    private void updatePriceSummary() {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        // Tính tổng giá ban đầu (chưa giảm giá)
        double subtotal = 0;
        double discountAmount = 0;

        for (CartItem item : shoppingCart.getCartItems()) {
            double itemOriginalTotal = item.getProduct().getPrice() * item.getQuantity();
            subtotal += itemOriginalTotal;

            // Tính số tiền được giảm giá
            if (item.getProduct().getDiscount() > 0) {
                double discountValue = itemOriginalTotal * (item.getProduct().getDiscount() / 100.0);
                discountAmount += discountValue;
            }
        }

        double total = subtotal - discountAmount;

        // Hiển thị các giá trị
        tvSubtotalValue.setText(currencyFormat.format(subtotal) + " đ");
        tvDiscountValue.setText("-" + currencyFormat.format(discountAmount) + " đ");
        tvTotalValue.setText(currencyFormat.format(total) + " đ");
    }

    private void processCheckout() {
        // Trong ứng dụng thực tế, phần này sẽ chuyển người dùng đến màn hình thanh toán
        Toast.makeText(this, "Đang chuyển đến trang thanh toán...", Toast.LENGTH_SHORT).show();

        // Thêm mã để chuyển đến màn hình thanh toán
        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }

    // CartAdapterListener interface callbacks
    @Override
    public void onItemRemoved(CartItem item) {
        shoppingCart.removeItem(item.getProduct().getId());
        updateCartUI();
        Toast.makeText(this, "Đã xóa " + item.getProduct().getName() + " khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        shoppingCart.updateItemQuantity(item.getProduct().getId(), newQuantity);
        updateCartUI();
    }
}