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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

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
        // Kiểm tra giỏ hàng có trống không
        if (shoppingCart.getCartItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống, vui lòng thêm sản phẩm trước khi thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra người dùng đã đăng nhập chưa
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Nếu chưa đăng nhập, hiển thị thông báo và chuyển đến màn hình đăng nhập
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục thanh toán", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, MainActivity.class);
            startActivity(loginIntent);
            return;
        }

        // Kiểm tra số lượng sản phẩm trong kho
        boolean insufficientStock = false;
        StringBuilder stockMessage = new StringBuilder("Sản phẩm không đủ số lượng trong kho:\n");

        for (CartItem item : shoppingCart.getCartItems()) {
            if (item.getQuantity() > item.getProduct().getStock()) {
                insufficientStock = true;
                stockMessage.append("- ")
                        .append(item.getProduct().getName())
                        .append(" (còn ")
                        .append(item.getProduct().getStock())
                        .append(")\n");
            }
        }

        if (insufficientStock) {
            Toast.makeText(this, stockMessage.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        // Nếu đã đăng nhập và giỏ hàng có sản phẩm, chuyển đến màn hình thanh toán
        Toast.makeText(this, "Đang chuyển đến trang thanh toán...", Toast.LENGTH_SHORT).show();

        // Chuyển sang màn hình thanh toán
        Intent intent = new Intent(this, CheckoutActivity.class);
        // Thiết lập BUY_NOW flag giống như nút "Mua ngay" để có cùng trải nghiệm
        intent.putExtra("BUY_NOW", true);

        // Lấy sản phẩm đầu tiên trong giỏ hàng để xử lý giống như nút "Mua ngay"
        CartItem firstItem = shoppingCart.getCartItems().get(0);
        intent.putExtra("PRODUCT_ID", firstItem.getProduct().getId());
        intent.putExtra("PRODUCT_QUANTITY", firstItem.getQuantity());

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