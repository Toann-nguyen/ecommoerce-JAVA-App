package com.example.ecommerce;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import models.CartItem;
import models.Order;
import models.Product;
import models.ShippingAddress;
import models.ShoppingCart;
import repository.FirebaseRepository;
import repository.OrderRepository;

public class CheckoutActivity extends AppCompatActivity {

    // UI components
    private MaterialToolbar topAppBar;
    private TextInputLayout tilFullName, tilPhone, tilAddress, tilCity;
    private TextInputEditText etFullName, etPhone, etAddress, etCity;
    private RadioGroup rgPaymentMethod;
    private TextView tvItemCount, tvSubtotal, tvDiscount, tvShippingFee, tvTotal;
    private Button btnPlaceOrder;

    // Data
    private ShoppingCart shoppingCart;
    private List<CartItem> cartItems;
    private double subtotal = 0;
    private double discount = 0;
    private double shippingFee = 30000; // Giá vận chuyển cố định 30,000 VNĐ
    private double total = 0;
    private OrderRepository orderRepository;
    private FirebaseUser currentUser;
    private FirebaseRepository productRepository;
    private boolean isBuyNow = false;
    private Product singleProduct;
    private int singleProductQuantity = 1;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Kiểm tra đăng nhập
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Khởi tạo Repository và Shopping Cart
        orderRepository = new OrderRepository();
        productRepository = new FirebaseRepository();
        shoppingCart = ShoppingCart.getInstance(this);

        // Kiểm tra xem có đang trong chế độ mua ngay không
        isBuyNow = getIntent().getBooleanExtra("BUY_NOW", false);

        if (isBuyNow) {
            // Xử lý mua ngay 1 sản phẩm
            String productId = getIntent().getStringExtra("PRODUCT_ID");
            singleProductQuantity = getIntent().getIntExtra("PRODUCT_QUANTITY", 1);

            if (productId != null) {
                productRepository.getProductById(productId, new FirebaseRepository.ProductCallback() {
                    @Override
                    public void onCallback(Product product) {
                        if (product != null) {
                            singleProduct = product;
                            // Tạo danh sách cartItems chỉ với 1 sản phẩm này
                            cartItems = new ArrayList<>();
                            cartItems.add(new CartItem(product, singleProductQuantity));
                            calculateOrderSummary();
                            displayOrderSummary();
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(CheckoutActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        } else {
            // Lấy dữ liệu từ giỏ hàng
            cartItems = shoppingCart.getCartItems();

            // Kiểm tra giỏ hàng có trống không
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            calculateOrderSummary();
            displayOrderSummary();
        }

        // Khởi tạo UI
        initViews();
        setupToolbar();

        // Thiết lập nút đặt hàng
        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        tilFullName = findViewById(R.id.tilFullName);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);
        tilCity = findViewById(R.id.tilCity);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void calculateOrderSummary() {
        int itemCount = 0;
        subtotal = 0;
        discount = 0;

        for (CartItem item : cartItems) {
            itemCount += item.getQuantity();
            double itemOriginalPrice = item.getProduct().getPrice() * item.getQuantity();
            subtotal += itemOriginalPrice;

            // Tính giảm giá dựa trên phần trăm giảm giá của sản phẩm
            if (item.getProduct().getDiscount() > 0) {
                double itemDiscount = itemOriginalPrice * (item.getProduct().getDiscount() / 100.0);
                discount += itemDiscount;
            }
        }

        // Tính tổng tiền
        total = subtotal - discount + shippingFee;
    }

    private void displayOrderSummary() {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        // Hiển thị số lượng sản phẩm
        int itemCount = 0;
        for (CartItem item : cartItems) {
            itemCount += item.getQuantity();
        }
        tvItemCount.setText("Tổng số sản phẩm: " + itemCount);

        // Hiển thị giá tiền
        tvSubtotal.setText(currencyFormat.format(subtotal) + " đ");
        tvDiscount.setText("-" + currencyFormat.format(discount) + " đ");
        tvShippingFee.setText(currencyFormat.format(shippingFee) + " đ");
        tvTotal.setText(currencyFormat.format(total) + " đ");
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Kiểm tra họ tên
        String fullName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Vui lòng nhập họ tên");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // Kiểm tra số điện thoại
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (phone.length() < 10) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        // Kiểm tra địa chỉ
        String address = etAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            tilAddress.setError("Vui lòng nhập địa chỉ");
            isValid = false;
        } else {
            tilAddress.setError(null);
        }

        // Kiểm tra thành phố
        String city = etCity.getText().toString().trim();
        if (TextUtils.isEmpty(city)) {
            tilCity.setError("Vui lòng nhập thành phố");
            isValid = false;
        } else {
            tilCity.setError(null);
        }

        return isValid;
    }

    private void validateAndPlaceOrder() {
        if (!validateInputs()) {
            return;
        }

        // Lấy thông tin địa chỉ giao hàng
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        ShippingAddress shippingAddress = new ShippingAddress(fullName, phone, address, city);

        // Lấy phương thức thanh toán
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        RadioButton selectedPaymentButton = findViewById(selectedPaymentId);
        String paymentMethod = selectedPaymentButton.getText().toString();

        // Tạo đối tượng đơn hàng
        Order order;
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Khách hàng";

            if (isBuyNow && singleProduct != null) {
                // Tạo đơn hàng cho trường hợp mua ngay 1 sản phẩm
                CartItem singleItem = new CartItem(singleProduct, singleProductQuantity);
                order = new Order(
                        currentUser.getUid(),
                        userEmail,
                        userName,
                        singleItem,
                        shippingAddress,
                        paymentMethod,
                        subtotal,
                        discount,
                        shippingFee
                );
            } else {
                // Tạo đơn hàng từ giỏ hàng
                order = new Order(
                        currentUser.getUid(),
                        cartItems,
                        shippingAddress,
                        paymentMethod,
                        subtotal,
                        discount,
                        shippingFee
                );
                order.setUserEmail(userEmail);
                order.setUserName(userName);
            }
        } else {
            // Fallback nếu không có thông tin người dùng
            order = new Order(
                    "guest",
                    cartItems,
                    shippingAddress,
                    paymentMethod,
                    subtotal,
                    discount,
                    shippingFee
            );
        }

        // Hiển thị dialog xác nhận
        showConfirmationDialog(order);
    }

    private void showConfirmationDialog(final Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt hàng")
                .setMessage("Bạn có chắc chắn muốn đặt đơn hàng này không?")
                .setPositiveButton("Đặt hàng", (dialog, which) -> {
                    placeOrder(order);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void placeOrder(final Order order) {
        // Hiển thị progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_dialog);
        progressDialog = builder.create();
        progressDialog.show();

        // Tạo đơn hàng trên Firebase
        orderRepository.createOrder(order, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(Order createdOrder) {
                onOrderSuccessful(createdOrder);
            }

            @Override
            public void onError(String errorMessage) {
                // Đóng progress dialog
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // Hiển thị thông báo lỗi
                Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onOrderSuccessful(Order createdOrder) {
        // Đóng progress dialog
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // Xóa giỏ hàng sau khi đặt hàng thành công từ giỏ hàng
        if (!isBuyNow) {
            shoppingCart.clearCart();
        }

        // Hiển thị thông báo thành công
        showOrderSuccessDialog(createdOrder.getId());
    }

    private void showOrderSuccessDialog(String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt hàng thành công")
                .setMessage("Đơn hàng của bạn đã được đặt thành công!\n\nMã đơn hàng: " + orderId)
                .setPositiveButton("Tiếp tục mua sắm", (dialog, which) -> {
                    // Chuyển về trang chủ
                    Intent intent = new Intent(CheckoutActivity.this, HomeMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}