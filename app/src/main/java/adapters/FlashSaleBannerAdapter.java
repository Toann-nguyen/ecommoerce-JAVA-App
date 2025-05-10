package adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import models.Product;

public class FlashSaleBannerAdapter extends RecyclerView.Adapter<FlashSaleBannerAdapter.FlashSaleBannerViewHolder> {

    private Context context;
    private List<Product> flashSaleProducts;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public FlashSaleBannerAdapter(Context context, List<Product> flashSaleProducts, OnProductClickListener listener) {
        this.context = context;
        this.flashSaleProducts = flashSaleProducts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashSaleBannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flash_sale_banner, parent, false);
        return new FlashSaleBannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleBannerViewHolder holder, int position) {
        Product product = flashSaleProducts.get(position);

        // Hiển thị tên sản phẩm
        holder.tvProductName.setText(product.getName());

        // Format giá và hiển thị
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String originalPrice = formatter.format(product.getPrice());
        String discountedPrice = formatter.format(product.getDiscountedPrice());

        holder.tvOriginalPrice.setText(originalPrice);
        holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        holder.tvDiscountPrice.setText(discountedPrice);

        // Hiển thị phần trăm giảm giá
        holder.tvDiscountPercent.setText("-" + product.getDiscount() + "%");

        // Hiển thị hình ảnh sản phẩm
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(product.getImageUrl())
                .centerCrop()
                .into(holder.imgProduct);
        }

        // Thêm sự kiện click
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashSaleProducts != null ? flashSaleProducts.size() : 0;
    }

    static class FlashSaleBannerViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvOriginalPrice;
        TextView tvDiscountPrice;
        TextView tvDiscountPercent;

        public FlashSaleBannerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscountPrice = itemView.findViewById(R.id.tvDiscountPrice);
            tvDiscountPercent = itemView.findViewById(R.id.tvDiscountPercent);
        }
    }
}