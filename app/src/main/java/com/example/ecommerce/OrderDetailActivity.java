package com.example.ecommerce;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import adapters.OrderDetailAdapter;
import models.CartItem;
import models.Order;
import models.OrderItem;

public class OrderDetailActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private TextView tvOrderId;
    private TextView tvOrderDate;
    private TextView tvOrderStatus;
    private TextView tvSubtotal;
    private TextView tvShippingFee;
    private TextView tvTotal;
    private TextView tvShippingAddress;
    private TextView tvPaymentMethod;
    private RecyclerView rvOrderItems;
    private OrderDetailAdapter orderDetailAdapter;

    private FirebaseFirestore db;
    private String orderId;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Get order ID from intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize formatters
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrderDetails();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvShippingAddress = findViewById(R.id.txtShippingAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        rvOrderItems = findViewById(R.id.recyclerOrderItems);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        orderDetailAdapter = new OrderDetailAdapter(this);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderDetailAdapter);
    }

    private void loadOrderDetails() {
        DocumentReference orderRef = db.collection("orders").document(orderId);
        orderRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Order order = documentSnapshot.toObject(Order.class);
                    if (order != null) {
                        displayOrderDetails(order);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void displayOrderDetails(Order order) {
        tvOrderId.setText("Đơn hàng #" + order.getId());
        tvOrderDate.setText(dateFormatter.format(order.getOrderDate()));
        tvOrderStatus.setText(getStatusText(order.getStatus()));
        tvSubtotal.setText(currencyFormatter.format(order.getSubtotal()));
        tvShippingFee.setText(currencyFormatter.format(order.getShippingFee()));        tvTotal.setText(currencyFormatter.format(order.getTotal()) + " đ");
        tvShippingAddress.setText(order.getShippingAddress() != null ? order.getShippingAddress().toString() : "N/A");
        tvPaymentMethod.setText(getPaymentMethodText(order.getPaymentMethod()));

        // Set status color
        int statusColor;
        switch (order.getStatus()) {
            case "pending":
                statusColor = getResources().getColor(R.color.status_pending);
                break;
            case "processing":
                statusColor = getResources().getColor(R.color.status_processing);
                break;
            case "completed":
                statusColor = getResources().getColor(R.color.status_completed);
                break;
            case "cancelled":
                statusColor = getResources().getColor(R.color.status_cancelled);
                break;
            default:
                statusColor = getResources().getColor(R.color.status_pending);
        }        tvOrderStatus.setTextColor(statusColor);

        // Convert CartItems to OrderItems and update adapter
        List<OrderItem> orderItems = new ArrayList<>();
        if (order.getItems() != null) {
            for (CartItem cartItem : order.getItems()) {
                OrderItem orderItem = new OrderItem(cartItem.getProduct(), cartItem.getQuantity());
                orderItems.add(orderItem);
            }
        }
        orderDetailAdapter.setOrderItems(orderItems);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "Chờ xác nhận";
            case "processing":
                return "Đang xử lý";
            case "completed":
                return "Đã hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    private String getPaymentMethodText(String method) {
        switch (method) {
            case "cod":
                return "Thanh toán khi nhận hàng (COD)";
            case "vnpay":
                return "Thanh toán qua VNPay";
            default:
                return method;
        }
    }
}
