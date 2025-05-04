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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import models.Product;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_FLASH_SALE = 1;
    public static final int TYPE_FEATURED = 2;

    private Context context;
    private List<Product> products;
    private ProductClickListener listener;
    private int viewType = TYPE_FEATURED; // Mặc định là featured

    public interface ProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products, ProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }
    public ProductAdapter(Context context, List<Product> products, ProductClickListener listener, int viewType) {
        this(context, products, listener);
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (this.viewType == TYPE_FLASH_SALE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_flash_sale, parent, false);
            return new FlashSaleViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_featured, parent, false);
            return new FeaturedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Product product = products.get(position);

        if (holder instanceof FlashSaleViewHolder) {
            FlashSaleViewHolder flashHolder = (FlashSaleViewHolder) holder;

            // Set product name
            flashHolder.tvNameFlash.setText(product.getName());

            // Set product price (sử dụng VND thay vì $)
            String priceText = String.format(Locale.getDefault(), "%,.0f VNĐ", product.getPrice());
            if (product.getDiscount() > 0) {
                double discountedPrice = product.getDiscountedPrice();
                priceText += String.format(Locale.getDefault(), " (-%d%%: %,.0f VNĐ)",
                        product.getDiscount(), discountedPrice);
            }
            flashHolder.tvPriceFlash.setText(priceText);

            // Load product image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.color.gray_placeholder)
                        .into(flashHolder.imgFlash);
            } else {
                flashHolder.imgFlash.setImageResource(R.color.gray_placeholder);
            }

            // Set click listeners
            flashHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            flashHolder.btnAddFlash.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                }
            });
        } else if (holder instanceof FeaturedViewHolder) {
            FeaturedViewHolder featuredHolder = (FeaturedViewHolder) holder;

            // Set product name
            featuredHolder.tvNameFeatured.setText(product.getName());

            // Set product price (sử dụng VND thay vì $)
            String priceText = String.format(Locale.getDefault(), "%,.0f VNĐ", product.getPrice());
            if (product.getDiscount() > 0) {
                double discountedPrice = product.getDiscountedPrice();
                priceText += String.format(Locale.getDefault(), " (-%d%%: %,.0f VNĐ)",
                        product.getDiscount(), discountedPrice);
            }
            featuredHolder.tvPriceFeatured.setText(priceText);

            // Load product image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.color.gray_placeholder)
                        .into(featuredHolder.imgFeatured);
            } else {
                featuredHolder.imgFeatured.setImageResource(R.color.gray_placeholder);
            }

            // Set click listeners
            featuredHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            featuredHolder.fabAddFeatured.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    static class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFlash;
        TextView tvNameFlash;
        TextView tvPriceFlash;
        MaterialButton btnAddFlash;

        public FlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFlash = itemView.findViewById(R.id.imgFlash);
            tvNameFlash = itemView.findViewById(R.id.tvNameFlash);
            tvPriceFlash = itemView.findViewById(R.id.tvPriceFlash);
            btnAddFlash = itemView.findViewById(R.id.btnAddFlash);
        }
    }

    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFeatured;
        TextView tvNameFeatured;
        TextView tvPriceFeatured;
        FloatingActionButton fabAddFeatured;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFeatured = itemView.findViewById(R.id.imgFeatured);
            tvNameFeatured = itemView.findViewById(R.id.tvNameFeatured);
            tvPriceFeatured = itemView.findViewById(R.id.tvPriceFeatured);
            fabAddFeatured = itemView.findViewById(R.id.fabAddFeatured);
        }
    }
}