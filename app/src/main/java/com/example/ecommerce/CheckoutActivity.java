package com.example.ecommerce;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
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

import com.example.ecommerce.payment.VNPayActivity;
import com.example.ecommerce.payment.VNPayHelper;

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

import com.example.ecommerce.payment.VNPayActivity;
import com.example.ecommerce.payment.VNPayHelper;
import com.example.ecommerce.dialogs.OrderCompleteDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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
    private ActivityResultLauncher<Intent> vnPayLauncher;

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
        setupVNPayLauncher();

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

        // Thêm phương thức thanh toán VNPAY vào RadioGroup nếu chưa có
        boolean hasVnpay = false;
        for (int i = 0; i < rgPaymentMethod.getChildCount(); i++) {
            View child = rgPaymentMethod.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                if ("VNPAY".equals(rb.getText().toString())) {
                    hasVnpay = true;
                    break;
                }
            }
        }

        if (!hasVnpay) {
            RadioButton rbVnpay = new RadioButton(this);
            rbVnpay.setId(View.generateViewId());
            rbVnpay.setText("VNPAY");
            rgPaymentMethod.addView(rbVnpay);
        }

        // Auto-populate the name field
        if (currentUser != null) {
            // Get user information from FirebaseUser
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            String name;

            // For Google login, use the display name
            if (displayName != null && !displayName.isEmpty()) {
                name = displayName;
            } else if (email != null && !email.isEmpty()) {
                // For email login, extract the name from email (e.g., "minhtoan" from "minhtoan@gmail.com")
                int atIndex = email.indexOf('@');
                if (atIndex > 0) {
                    name = email.substring(0, atIndex);
                } else {
                    name = email;
                }
            } else {
                name = "Khách hàng";
            }

            // Set the name in the field
            etFullName.setText(name);
            // Keep the name field enabled but show it's auto-populated
            tilFullName.setHint("Họ và tên (Tự động điền)");
        }
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupVNPayLauncher() {
        vnPayLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String vnpayResponse = result.getData().getStringExtra("VNPAY_RESPONSE");
                        handleVNPayResponse(vnpayResponse);
                    } else {
                        // Payment canceled or failed
                        Toast.makeText(this, "Thanh toán bị hủy hoặc thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
        );
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

        // Kiểm tra nếu thanh toán bằng VNPAY
        if ("VNPAY".equals(paymentMethod)) {
            // Tạo mã đơn hàng duy nhất cho VNPAY
            String orderId = "ECM" + System.currentTimeMillis();
            String orderInfo = "Thanh toan don hang #" + orderId;

            // Tạo URL thanh toán VNPAY
            String paymentUrl = VNPayHelper.generatePaymentUrl(
                    this,
                    orderId,
                    total,  // Số tiền thanh toán (VND)
                    orderInfo
            );

            // Mở VNPayActivity để thanh toán
            Intent intent = new Intent(this, VNPayActivity.class);
            intent.putExtra("PAYMENT_URL", paymentUrl);
            vnPayLauncher.launch(intent);
        } else {
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_complete_review, null);

        TextView tvOrderId = dialogView.findViewById(R.id.tvOrderId);
        Button btnContinueShopping = dialogView.findViewById(R.id.btnContinueShopping);
        Button btnReviewProducts = dialogView.findViewById(R.id.btnReviewProducts);

        // Configurar ID de pedido
        tvOrderId.setText("Mã đơn hàng: #" + orderId);

        // Crear diálogo
        AlertDialog dialog = builder.setView(dialogView).setCancelable(false).create();

        // Configurar botones
        btnContinueShopping.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, HomeMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnReviewProducts.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, ProductReviewActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            intent.putExtra("DIRECT_REVIEW", true);
            startActivity(intent);
            finish();
        });

        // Mostrar diálogo
        dialog.show();
    }

    private void handleVNPayResponse(String responseUrl) {
        // Parse URL to get response parameters
        Uri uri = Uri.parse(responseUrl);
        String vnpResponseCode = uri.getQueryParameter("vnp_ResponseCode");

        if ("00".equals(vnpResponseCode)) {
            // Payment successful
            // Lấy thông tin để tạo đơn hàng
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            ShippingAddress shippingAddress = new ShippingAddress(fullName, phone, address, city);

            // Tạo đối tượng đơn hàng với phương thức thanh toán là VNPAY
            Order order;
            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Khách hàng";
                String transactionId = uri.getQueryParameter("vnp_TransactionNo");

                if (isBuyNow && singleProduct != null) {
                    CartItem singleItem = new CartItem(singleProduct, singleProductQuantity);
                    order = new Order(
                            currentUser.getUid(),
                            userEmail,
                            userName,
                            singleItem,
                            shippingAddress,
                            "VNPAY - " + transactionId,
                            subtotal,
                            discount,
                            shippingFee
                    );
                } else {
                    order = new Order(
                            currentUser.getUid(),
                            cartItems,
                            shippingAddress,
                            "VNPAY - " + transactionId,
                            subtotal,
                            discount,
                            shippingFee
                    );
                    order.setUserEmail(userEmail);
                    order.setUserName(userName);
                }

                // Lưu đơn hàng vào database
                saveOrderAfterPayment(order);
            }
        } else {
            // Payment failed
            Toast.makeText(this, "Thanh toán thất bại! Mã lỗi: " + vnpResponseCode, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderAfterPayment(Order order) {
        // Hiển thị progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_dialog);
        progressDialog = builder.create();
        progressDialog.show();

        // Lưu đơn hàng vào Firebase
        orderRepository.createOrder(order, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(Order createdOrder) {
                onOrderSuccessful(createdOrder);
            }

            @Override
            public void onError(String errorMessage) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
