package com.example.ecommerce.admin;

import android.graphics.Color;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.google.android.material.appbar.MaterialToolbar;

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}