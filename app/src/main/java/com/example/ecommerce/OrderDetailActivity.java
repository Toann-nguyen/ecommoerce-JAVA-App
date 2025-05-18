package com.example.ecommerce;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private MaterialToolbar toolbar;
    private TextView txtOrderId;
    private TextView txtOrderDate;
    private TextView txtOrderStatus;
    private TextView txtSubtotal;
    private TextView txtShippingFee;
    private TextView txtTotal;
    private TextView txtShippingAddress;
    private TextView txtPaymentMethod;
    private TextView txtDiscount;
    private TextView txtRecipientName;
    private TextView txtRecipientPhone;
    private TextView txtCustomerName;
    private TextView txtCustomerEmail;
    private RecyclerView rvOrderItems;
    private CardView cardUpdateStatus;
    private CardView cardStatisticsReport;
    private RadioGroup radioGroupStatus;
    private Button btnUpdateStatus;
    private Button btnGenerateReport;
    private OrderDetailAdapter orderDetailAdapter;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String orderId;
    private boolean isAdmin = false;
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

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupAdminFeatures();
        loadOrderDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtTotal = findViewById(R.id.txtTotal);
        txtShippingAddress = findViewById(R.id.txtShippingAddress);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtRecipientName = findViewById(R.id.txtRecipientName);
        txtRecipientPhone = findViewById(R.id.txtRecipientPhone);
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);
        rvOrderItems = findViewById(R.id.recyclerOrderItems);
        cardUpdateStatus = findViewById(R.id.cardUpdateStatus);
        cardStatisticsReport = findViewById(R.id.cardStatisticsReport);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupRecyclerView() {
        orderDetailAdapter = new OrderDetailAdapter(this);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderDetailAdapter);
    }

    private void setupAdminFeatures() {
        if (isAdmin) {
            if (cardUpdateStatus != null) {
                cardUpdateStatus.setVisibility(View.VISIBLE);
            }
            if (cardStatisticsReport != null) {
                cardStatisticsReport.setVisibility(View.VISIBLE);
            }
            setupUpdateStatus();
            setupGenerateReport();
        } else {
            if (cardUpdateStatus != null) {
                cardUpdateStatus.setVisibility(View.GONE);
            }
            if (cardStatisticsReport != null) {
                cardStatisticsReport.setVisibility(View.GONE);
            }
        }
    }

    private void setupUpdateStatus() {
        if (btnUpdateStatus != null) {
            btnUpdateStatus.setOnClickListener(v -> {
                String newStatus = getSelectedStatus();
                if (newStatus != null) {
                    updateOrderStatus(newStatus);
                } else {
                    Toast.makeText(this, "Vui lòng chọn trạng thái", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getSelectedStatus() {
        if (radioGroupStatus == null) return null;
        int checkedId = radioGroupStatus.getCheckedRadioButtonId();
        if (checkedId == R.id.radioPending) {
            return Order.STATUS_PENDING;
        } else if (checkedId == R.id.radioConfirmed) {
            return Order.STATUS_CONFIRMED;
        } else if (checkedId == R.id.radioShipping) {
            return Order.STATUS_SHIPPING;
        } else if (checkedId == R.id.radioDelivered) {
            return Order.STATUS_DELIVERED;
        } else if (checkedId == R.id.radioCancelled) {
            return Order.STATUS_CANCELLED;
        }
        return null;
    }

    private void updateOrderStatus(String newStatus) {
        DocumentReference orderRef = db.collection("orders").document(orderId);
        orderRef.update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                    if (txtOrderStatus != null) {
                        txtOrderStatus.setText(getStatusText(newStatus));
                        int statusColor;
                        switch (newStatus) {
                            case Order.STATUS_PENDING:
                                statusColor = getResources().getColor(R.color.status_pending);
                                break;
                            case Order.STATUS_CONFIRMED:
                            case Order.STATUS_SHIPPING:
                                statusColor = getResources().getColor(R.color.status_processing);
                                break;
                            case Order.STATUS_DELIVERED:
                                statusColor = getResources().getColor(R.color.status_completed);
                                break;
                            case Order.STATUS_CANCELLED:
                                statusColor = getResources().getColor(R.color.status_cancelled);
                                break;
                            default:
                                statusColor = getResources().getColor(R.color.status_pending);
                        }
                        txtOrderStatus.setTextColor(statusColor);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupGenerateReport() {
        if (btnGenerateReport != null) {
            btnGenerateReport.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng xuất báo cáo đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }
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
                    Toast.makeText(this, "Lỗi khi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayOrderDetails(Order order) {
        if (txtOrderId != null) {
            txtOrderId.setText("Đơn hàng #" + order.getId());
        }
        if (txtOrderDate != null) {
            txtOrderDate.setText(order.getOrderDate() != null ? dateFormatter.format(order.getOrderDate()) : "N/A");
        }
        if (txtOrderStatus != null) {
            txtOrderStatus.setText(getStatusText(order.getStatus()));
        }
        if (txtSubtotal != null) {
            txtSubtotal.setText(currencyFormatter.format(order.getSubtotal()));
        }
        if (txtShippingFee != null) {
            txtShippingFee.setText(currencyFormatter.format(order.getShippingFee()));
        }
        if (txtTotal != null) {
            txtTotal.setText(currencyFormatter.format(order.getTotal()) + " đ");
        }
        if (txtDiscount != null) {
            txtDiscount.setText(currencyFormatter.format(order.getDiscount()));
        }
        if (txtShippingAddress != null) {
            txtShippingAddress.setText(order.getShippingAddress() != null ? order.getShippingAddress().toString() : "N/A");
        }
        if (txtPaymentMethod != null) {
            txtPaymentMethod.setText(getPaymentMethodText(order.getPaymentMethod()));
        }
        if (txtCustomerName != null) {
            txtCustomerName.setText(order.getUserName() != null ? order.getUserName() : "N/A");
        }
        if (txtCustomerEmail != null) {
            txtCustomerEmail.setText(order.getUserEmail() != null ? order.getUserEmail() : "N/A");
        }
        if (txtRecipientName != null) {
            txtRecipientName.setText(order.getUserName() != null ? order.getUserName() : "N/A");
        }
        if (txtRecipientPhone != null) {
            txtRecipientPhone.setText("N/A");
        }

        if (txtOrderStatus != null) {
            int statusColor;
            switch (order.getStatus()) {
                case Order.STATUS_PENDING:
                    statusColor = getResources().getColor(R.color.status_pending);
                    break;
                case Order.STATUS_CONFIRMED:
                case Order.STATUS_SHIPPING:
                    statusColor = getResources().getColor(R.color.status_processing);
                    break;
                case Order.STATUS_DELIVERED:
                    statusColor = getResources().getColor(R.color.status_completed);
                    break;
                case Order.STATUS_CANCELLED:
                    statusColor = getResources().getColor(R.color.status_cancelled);
                    break;
                default:
                    statusColor = getResources().getColor(R.color.status_pending);
            }
            txtOrderStatus.setTextColor(statusColor);
        }

        if (radioGroupStatus != null && isAdmin) {
            switch (order.getStatus()) {
                case Order.STATUS_PENDING:
                    radioGroupStatus.check(R.id.radioPending);
                    break;
                case Order.STATUS_CONFIRMED:
                    radioGroupStatus.check(R.id.radioConfirmed);
                    break;
                case Order.STATUS_SHIPPING:
                    radioGroupStatus.check(R.id.radioShipping);
                    break;
                case Order.STATUS_DELIVERED:
                    radioGroupStatus.check(R.id.radioDelivered);
                    break;
                case Order.STATUS_CANCELLED:
                    radioGroupStatus.check(R.id.radioCancelled);
                    break;
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        if (order.getItems() != null) {
            for (CartItem cartItem : order.getItems()) {
                OrderItem orderItem = new OrderItem(cartItem.getProduct(), cartItem.getQuantity());
                orderItems.add(orderItem);
            }
        }
        if (orderDetailAdapter != null) {
            orderDetailAdapter.setOrderItems(orderItems);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case Order.STATUS_PENDING:
                return "Chờ xác nhận";
            case Order.STATUS_CONFIRMED:
                return "Đã xác nhận";
            case Order.STATUS_SHIPPING:
                return "Đang giao hàng";
            case Order.STATUS_DELIVERED:
                return "Đã giao hàng";
            case Order.STATUS_CANCELLED:
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
