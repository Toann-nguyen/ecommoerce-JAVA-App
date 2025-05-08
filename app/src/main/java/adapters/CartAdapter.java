package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private List<CartItem> cartItems;
    private final CartAdapterListener listener;

    // Interface để xử lý các sự kiện từ adapter
    public interface CartAdapterListener {
        void onItemRemoved(CartItem item);

        void onQuantityChanged(CartItem item, int newQuantity);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartAdapterListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProductImage;
        private final TextView tvProductName;
        private final TextView tvProductPrice;
        private final TextView tvDiscountPrice;
        private final TextView tvQuantity;
        private final TextView tvItemTotal;
        private final Button btnIncrease;
        private final Button btnDecrease;
        private final ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvDiscountPrice = itemView.findViewById(R.id.tvDiscountPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem cartItem) {
            // Hiển thị thông tin sản phẩm
            tvProductName.setText(cartItem.getProduct().getName());

            // Format giá tiền
            NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            String price = currencyFormat.format(cartItem.getProduct().getPrice()) + " đ";
            tvProductPrice.setText(price);

            // Hiển thị giảm giá nếu có
            int discount = cartItem.getProduct().getDiscount();
            if (discount > 0) {
                tvDiscountPrice.setVisibility(View.VISIBLE);
                tvDiscountPrice.setText("Giảm " + discount + "%");
            } else {
                tvDiscountPrice.setVisibility(View.GONE);
            }

            // Hiển thị số lượng
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));

            // Hiển thị tổng tiền cho item này
            String totalPrice = currencyFormat.format(cartItem.getTotalPrice()) + " đ";
            tvItemTotal.setText(totalPrice);

            // Load ảnh sản phẩm
            if (cartItem.getProduct().getImageUrl() != null && !cartItem.getProduct().getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(cartItem.getProduct().getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_broken_image)
                        .into(ivProductImage);
            }

            // Xử lý sự kiện tăng số lượng
            btnIncrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() + 1;
                if (newQuantity <= 99) { // Giới hạn số lượng tối đa
                    listener.onQuantityChanged(cartItem, newQuantity);
                }
            });

            // Xử lý sự kiện giảm số lượng
            btnDecrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() - 1;
                if (newQuantity >= 1) { // Số lượng tối thiểu là 1
                    listener.onQuantityChanged(cartItem, newQuantity);
                }
            });

            // Xử lý sự kiện xóa sản phẩm
            btnRemove.setOnClickListener(v -> listener.onItemRemoved(cartItem));
        }
    }
}