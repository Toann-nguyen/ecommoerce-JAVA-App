package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import models.Order;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private OrderItemClickListener listener;
    private SimpleDateFormat dateFormat;
    private NumberFormat currencyFormat;

    public interface OrderItemClickListener {
        void onOrderClick(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orders, OrderItemClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        this.currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView orderCard;
        private TextView txtOrderId;
        private TextView txtOrderDate;
        private TextView txtCustomerName;
        private TextView txtCustomerEmail;
        private TextView txtItemCount;
        private TextView txtOrderTotal;
        private TextView txtOrderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderCard = itemView.findViewById(R.id.orderCard);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
            txtCustomerEmail = itemView.findViewById(R.id.txtCustomerEmail);
            txtItemCount = itemView.findViewById(R.id.txtItemCount);
            txtOrderTotal = itemView.findViewById(R.id.txtOrderTotal);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
        }

        void bind(final Order order) {
            // Set basic order details
            txtOrderId.setText(order.getId() != null ? order.getId() : "N/A");
            txtOrderDate.setText(order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "N/A");

            // Set customer info
            String userName = order.getUserName();
            if (userName == null || userName.trim().isEmpty()) {
                userName = "Khách hàng";
            }
            txtCustomerName.setText(userName);
            txtCustomerEmail.setText(order.getUserEmail() != null ? order.getUserEmail() : "N/A");

            // Set items count
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            txtItemCount.setText(itemCount + " sản phẩm");

            // Set total
            try {
                String totalFormatted = currencyFormat.format(order.getTotal()) + " đ";
                txtOrderTotal.setText(totalFormatted);
            } catch (Exception e) {
                txtOrderTotal.setText("N/A");
            }

            // Set status with appropriate color
            setupOrderStatus(txtOrderStatus, order.getStatus());

            // Set click listener
            orderCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        private void setupOrderStatus(TextView statusView, String status) {
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

            statusView.setText(statusText);
            statusView.getBackground().setTint(statusColor);
        }
    }
}