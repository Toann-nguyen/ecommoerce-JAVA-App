package com.example.ecommerce.admin;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import adapters.OrderItemsAdapter;
import models.CartItem;
import models.Order;
import models.ShippingAddress;
import repository.AdminFirebaseRepository;
import utils.PermissionManager;

public class OrderDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView txtOrderId, txtOrderDate, txtOrderStatus;
    private TextView txtCustomerName, txtCustomerEmail, txtPaymentMethod;
    private TextView txtRecipientName, txtRecipientPhone, txtShippingAddress;
    private RecyclerView recyclerOrderItems;
    private TextView txtSubtotal, txtDiscount, txtShippingFee, txtTotal;
    private RadioGroup radioGroupStatus;
    private RadioButton radioPending, radioConfirmed, radioShipping, radioDelivered, radioCancelled;
    private Button btnUpdateStatus;
    private ProgressBar progressBar;

    private AdminFirebaseRepository repository;
    private PermissionManager permissionManager;
    private Order order;
    private String orderId;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Khởi tạo Permission Manager
        permissionManager = PermissionManager.getInstance();

        // Kiểm tra quyền admin
        if (!permissionManager.isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập khu vực này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy order ID từ intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo số tiền và định dạng ngày
        currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

        // Khởi tạo Firebase Repository
        repository = new AdminFirebaseRepository();

        // Khởi tạo UI components
        initViews();
        setupToolbar();
        setupStatusRadioGroup();

        // Tải thông tin đơn hàng
        loadOrderDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtRecipientName = findViewById(R.id.txtRecipientName);
        txtRecipientPhone = findViewById(R.id.txtRecipientPhone);
        txtShippingAddress = findViewById(R.id.txtShippingAddress);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtTotal = findViewById(R.id.txtTotal);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        radioPending = findViewById(R.id.radioPending);
        radioConfirmed = findViewById(R.id.radioConfirmed);
        radioShipping = findViewById(R.id.radioShipping);
        radioDelivered = findViewById(R.id.radioDelivered);
        radioCancelled = findViewById(R.id.radioCancelled);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        progressBar = findViewById(R.id.progressBar);

        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));

        btnUpdateStatus.setOnClickListener(v -> updateOrderStatus());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
    }

    private void setupStatusRadioGroup() {
        // Map radio buttons to order status
        radioPending.setTag(Order.STATUS_PENDING);
        radioConfirmed.setTag(Order.STATUS_CONFIRMED);
        radioShipping.setTag(Order.STATUS_SHIPPING);
        radioDelivered.setTag(Order.STATUS_DELIVERED);
        radioCancelled.setTag(Order.STATUS_CANCELLED);
    }

    private void loadOrderDetails() {
        showProgressBar(true);

        repository.getOrderById(orderId, new AdminFirebaseRepository.OrderCallback() {
            @Override
            public void onCallback(Order loadedOrder) {
                showProgressBar(false);
                if (loadedOrder != null) {
                    order = loadedOrder;
                    displayOrderDetails();
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showProgressBar(false);
                Toast.makeText(OrderDetailActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrderDetails() {
        if (order == null) return;

        // Order details
        txtOrderId.setText(order.getId());
        txtOrderDate.setText(order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "N/A");

        // Update status text and select correct radio button
        updateStatusDisplay(order.getStatus());
        selectRadioButtonByStatus(order.getStatus());

        // Customer info
        txtCustomerName.setText(order.getUserName() != null ? order.getUserName() : "N/A");
        txtCustomerEmail.setText(order.getUserEmail() != null ? order.getUserEmail() : "N/A");
        txtPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A");

        // Shipping info
        ShippingAddress address = order.getShippingAddress();
        if (address != null) {
            txtRecipientName.setText(address.getFullName() != null ? address.getFullName() : "N/A");
            txtRecipientPhone.setText(address.getPhone() != null ? address.getPhone() : "N/A");
            txtShippingAddress.setText(formatAddress(address));
        } else {
            txtRecipientName.setText("N/A");
            txtRecipientPhone.setText("N/A");
            txtShippingAddress.setText("N/A");
        }

        // Setup order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            OrderItemsAdapter adapter = new OrderItemsAdapter(this, order.getItems());
            recyclerOrderItems.setAdapter(adapter);
        }

        // Order totals
        txtSubtotal.setText(currencyFormat.format(order.getSubtotal()) + " đ");
        txtDiscount.setText("-" + currencyFormat.format(order.getDiscount()) + " đ");
        txtShippingFee.setText(currencyFormat.format(order.getShippingFee()) + " đ");
        txtTotal.setText(currencyFormat.format(order.getTotal()) + " đ");
    }

    private String formatAddress(ShippingAddress address) {
        if (address == null) return "N/A";

        // Use the address and city fields from ShippingAddress
        String addressStr = address.getAddress();
        String city = address.getCity();

        if (addressStr != null && !addressStr.isEmpty()) {
            if (city != null && !city.isEmpty()) {
                return addressStr + ", " + city;
            }
            return addressStr;
        }
        return "N/A";
    }

    private void updateStatusDisplay(String status) {
        String statusText;
        int statusColor;

        switch (status) {
            case Order.STATUS_PENDING:
                statusText = "Chờ xác nhận";
                statusColor = Color.parseColor("#FF9800"); // Orange
                break;
            case Order.STATUS_CONFIRMED:
                statusText = "Đã xác nhận";
                statusColor = Color.parseColor("#2196F3"); // Blue
                break;
            case Order.STATUS_SHIPPING:
                statusText = "Đang giao";
                statusColor = Color.parseColor("#673AB7"); // Purple
                break;
            case Order.STATUS_DELIVERED:
                statusText = "Đã giao";
                statusColor = Color.parseColor("#4CAF50"); // Green
                break;
            case Order.STATUS_CANCELLED:
                statusText = "Đã hủy";
                statusColor = Color.parseColor("#F44336"); // Red
                break;
            default:
                statusText = "Không xác định";
                statusColor = Color.parseColor("#607D8B"); // Gray
                break;
        }

        txtOrderStatus.setText(statusText);
        txtOrderStatus.getBackground().setTint(statusColor);
    }

    private void selectRadioButtonByStatus(String status) {
        switch (status) {
            case Order.STATUS_PENDING:
                radioPending.setChecked(true);
                break;
            case Order.STATUS_CONFIRMED:
                radioConfirmed.setChecked(true);
                break;
            case Order.STATUS_SHIPPING:
                radioShipping.setChecked(true);
                break;
            case Order.STATUS_DELIVERED:
                radioDelivered.setChecked(true);
                break;
            case Order.STATUS_CANCELLED:
                radioCancelled.setChecked(true);
                break;
        }
    }

    private void updateOrderStatus() {
        int selectedId = radioGroupStatus.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);

        if (radioButton == null) {
            Toast.makeText(this, "Vui lòng chọn trạng thái", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatus = radioButton.getTag().toString();

        // Skip if status hasn't changed
        if (order.getStatus().equals(newStatus)) {
            Toast.makeText(this, "Trạng thái không thay đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar(true);
        repository.updateOrderStatus(order.getId(), newStatus, new AdminFirebaseRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                showProgressBar(false);
                order.setStatus(newStatus);
                updateStatusDisplay(newStatus);
                Toast.makeText(OrderDetailActivity.this,
                        "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                showProgressBar(false);
                Toast.makeText(OrderDetailActivity.this,
                        "Lỗi cập nhật trạng thái: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_generate_receipt) {
            generateReceipt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Generate a simple text receipt for the order
     */
    private void generateReceipt() {
        // Check for storage permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
            return;
        }

        if (order == null) {
            Toast.makeText(this, "Không thể tạo hóa đơn: Không có thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("===============================\n");
        receipt.append("          HÓA ĐƠN BÁN HÀNG\n");
        receipt.append("===============================\n\n");

        // Order info
        receipt.append("Mã đơn hàng: ").append(order.getId()).append("\n");
        receipt.append("Ngày đặt: ").append(dateFormat.format(order.getOrderDate())).append("\n");
        receipt.append("Trạng thái: ").append(getStatusText(order.getStatus())).append("\n\n");

        // Customer info
        receipt.append("THÔNG TIN KHÁCH HÀNG:\n");
        receipt.append("Tên: ").append(order.getUserName()).append("\n");
        receipt.append("Email: ").append(order.getUserEmail()).append("\n\n");

        // Shipping info
        ShippingAddress address = order.getShippingAddress();
        if (address != null) {
            receipt.append("THÔNG TIN GIAO HÀNG:\n");
            receipt.append("Người nhận: ").append(address.getFullName()).append("\n");
            receipt.append("Số điện thoại: ").append(address.getPhone()).append("\n");
            receipt.append("Địa chỉ: ").append(formatAddress(address)).append("\n\n");
        }

        // Products
        receipt.append("DANH SÁCH SẢN PHẨM:\n");
        receipt.append("-------------------------------\n");
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    receipt.append(item.getProduct().getName())
                            .append(" x").append(item.getQuantity())
                            .append(" = ").append(currencyFormat.format(item.getProduct().getPrice() * item.getQuantity())).append(" đ\n");
                }
            }
        }
        receipt.append("-------------------------------\n\n");

        // Totals
        receipt.append("TỔNG TIỀN:\n");
        receipt.append("Tạm tính: ").append(currencyFormat.format(order.getSubtotal())).append(" đ\n");
        receipt.append("Giảm giá: -").append(currencyFormat.format(order.getDiscount())).append(" đ\n");
        receipt.append("Phí vận chuyển: ").append(currencyFormat.format(order.getShippingFee())).append(" đ\n");
        receipt.append("-------------------------------\n");
        receipt.append("TỔNG CỘNG: ").append(currencyFormat.format(order.getTotal())).append(" đ\n\n");

        // Payment method
        receipt.append("Phương thức thanh toán: ").append(order.getPaymentMethod()).append("\n\n");
        receipt.append("===============================\n");
        receipt.append("Cảm ơn quý khách đã mua hàng!\n");
        receipt.append("===============================\n");

        try {
            // Create a file in Downloads directory
            File receiptFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Receipt_" + order.getId() + ".txt");

            FileOutputStream fos = new FileOutputStream(receiptFile);
            fos.write(receipt.toString().getBytes());
            fos.close();

            Toast.makeText(this, "Đã lưu hóa đơn vào thư mục Downloads", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Không thể tạo hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, generate receipt
                generateReceipt();
            } else {
                Toast.makeText(this, "Không thể tạo hóa đơn: Không có quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Convert status code to readable text
     */
    private String getStatusText(String status) {
        switch (status) {
            case Order.STATUS_PENDING:
                return "Chờ xác nhận";
            case Order.STATUS_CONFIRMED:
                return "Đã xác nhận";
            case Order.STATUS_SHIPPING:
                return "Đang giao";
            case Order.STATUS_DELIVERED:
                return "Đã giao";
            case Order.STATUS_CANCELLED:
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }
}