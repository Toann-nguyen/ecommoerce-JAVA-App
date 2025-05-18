package com.example.ecommerce.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.example.ecommerce.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SimpleProductAdapter extends RecyclerView.Adapter<SimpleProductAdapter.ViewHolder> {
    private List<Product> products;

    public SimpleProductAdapter(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        
        holder.tvProductName.setText(product.getName());
        
        // Format price to Vietnamese currency
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String price = formatter.format(product.getPrice()) + "đ";
        holder.tvPrice.setText(price);
        
        holder.tvQuantity.setText("Số lượng: " + product.getQuantity());
        
        // Load image using Glide
        Glide.with(holder.ivProduct.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.ivProduct);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
