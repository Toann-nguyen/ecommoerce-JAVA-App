package adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
    private final List<Order> orders;
    private final NumberFormat currencyFormatter;
    private final SimpleDateFormat dateFormatter;
    private OnItemClickListener itemClickListener;

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvOrderDate;
        private final TextView tvOrderStatus;
        private final TextView tvOrderTotal;

        public OrderViewHolder(View view) {
            super(view);
            tvOrderId = view.findViewById(R.id.tvOrderId);
            tvOrderDate = view.findViewById(R.id.tvOrderDate);
            tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = view.findViewById(R.id.tvOrderTotal);
        }

        public void bind(Order order, Context context, NumberFormat currencyFormatter, SimpleDateFormat dateFormatter) {
            tvOrderId.setText("Đơn hàng #" + order.getId());
            tvOrderDate.setText(dateFormatter.format(order.getOrderDate()));
            tvOrderStatus.setText(getStatusText(order.getStatus()));
            tvOrderTotal.setText(currencyFormatter.format(order.getTotal()) + " đ");

            // Set status color
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

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, context, currencyFormatter, dateFormatter);
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(order);
            } else {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("ORDER_ID", order.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}