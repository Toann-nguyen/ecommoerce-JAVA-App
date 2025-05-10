package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import models.CartItem;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {
    private Context context;
    private List<CartItem> items;
    private NumberFormat currencyFormat;

    public OrderItemsAdapter(Context context, List<CartItem> items) {
        this.context = context;
        this.items = items;
        this.currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView txtProductName, txtProductPrice, txtProductQuantity;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductQuantity = itemView.findViewById(R.id.txtProductQuantity);
        }

        void bind(CartItem item) {
            if (item.getProduct() != null) {
                // Set product name
                txtProductName.setText(item.getProduct().getName());

                // Set product price
                String priceFormatted = currencyFormat.format(item.getProduct().getPrice()) + " đ";
                txtProductPrice.setText(priceFormatted);

                // Set quantity
                txtProductQuantity.setText("x" + item.getQuantity());

                // Load product image
                if (item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()) {
                    Glide.with(context)
                            .load(item.getProduct().getImageUrl())
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .centerCrop()
                            .into(imgProduct);
                } else {
                    imgProduct.setImageResource(R.drawable.placeholder_image);
                }
            }
        }
    }
}