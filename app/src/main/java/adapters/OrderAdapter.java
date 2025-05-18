package adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.OrderDetailActivity;
import com.example.ecommerce.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import models.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private final NumberFormat currencyFormatter;
    private final SimpleDateFormat dateFormatter;
    private OnOrderClickListener listener;

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
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

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public void setItemClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId;
        private TextView tvOrderDate;
        private TextView tvOrderStatus;
        private TextView tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Order order = orders.get(position);
                    Intent intent = new Intent(context, OrderDetailActivity.class);
                    intent.putExtra("ORDER_ID", order.getId());
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Order order) {
            tvOrderId.setText("Đơn hàng #" + order.getId());
            tvOrderDate.setText(dateFormatter.format(order.getOrderDate()));
            tvOrderStatus.setText(getStatusText(order.getStatus()));
            tvOrderTotal.setText(currencyFormatter.format(order.getTotal()) + " đ");

            // Set status color based on order status
            int statusColor;
            switch (order.getStatus()) {
                case "pending":
                    statusColor = context.getResources().getColor(R.color.status_pending);
                    break;
                case "processing":
                    statusColor = context.getResources().getColor(R.color.status_processing);
                    break;
                case "completed":
                    statusColor = context.getResources().getColor(R.color.status_completed);
                    break;
                case "cancelled":
                    statusColor = context.getResources().getColor(R.color.status_cancelled);
                    break;
                default:
                    statusColor = context.getResources().getColor(R.color.status_pending);
            }
            tvOrderStatus.setTextColor(statusColor);
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
    }
}
