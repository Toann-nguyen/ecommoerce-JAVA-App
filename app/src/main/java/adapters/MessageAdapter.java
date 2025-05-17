package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.Message;
import models.Product;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_PRODUCT = 2;

    private List<Message> messages;
    private Context context;
    private OnProductClickListener productClickListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public MessageAdapter(Context context, List<Message> messages, OnProductClickListener listener) {
        this.context = context;
        this.messages = messages;
        this.productClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.isProductMessage() ? VIEW_TYPE_PRODUCT : VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PRODUCT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_product, parent, false);
            return new ProductMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
            return new TextMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean isAdmin = "admin".equals(message.getSenderId());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(message.getTimestamp()));

        if (holder instanceof ProductMessageViewHolder) {
            setupProductMessage((ProductMessageViewHolder) holder, message, isAdmin, formattedTime);
        } else {
            setupTextMessage((TextMessageViewHolder) holder, message, isAdmin, formattedTime);
        }
    }

    private void setupProductMessage(ProductMessageViewHolder holder, Message message, boolean isAdmin, String time) {
        // Set message text if any
        if (message.getMessageText() != null && !message.getMessageText().isEmpty()) {
            holder.textMessage.setVisibility(View.VISIBLE);
            holder.textMessage.setText(message.getMessageText());
        } else {
            holder.textMessage.setVisibility(View.GONE);
        }

        // Set colors based on sender
        holder.cardViewMessage.setCardBackgroundColor(ContextCompat.getColor(context, 
            isAdmin ? R.color.admin_message_bg : R.color.user_message_bg));
        holder.textMessage.setTextColor(ContextCompat.getColor(context, 
            isAdmin ? R.color.message_text_color_admin : R.color.message_text_color_user));
        holder.textTime.setTextColor(ContextCompat.getColor(context,
            isAdmin ? R.color.message_text_color_admin : R.color.message_text_color_user));

        // Set product details
        Product product = message.getProduct();
        if (product != null) {
            holder.txtProductName.setText(product.getName());
            
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = currencyFormat.format(product.getPrice());
            holder.txtProductPrice.setText(formattedPrice);

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.imgProduct);
            }

            // Set click listener for product card
            holder.cardViewProduct.setOnClickListener(v -> {
                if (productClickListener != null) {
                    productClickListener.onProductClick(product);
                }
            });
        }

        // Set message time
        holder.textTime.setText(time);

        // Update layout alignment
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardViewMessage.getLayoutParams();
        if (isAdmin) {
            params.leftMargin = (int) context.getResources().getDimension(R.dimen.message_margin_large);
            params.rightMargin = (int) context.getResources().getDimension(R.dimen.message_margin_small);
        } else {
            params.leftMargin = (int) context.getResources().getDimension(R.dimen.message_margin_small);
            params.rightMargin = (int) context.getResources().getDimension(R.dimen.message_margin_large);
        }
        holder.cardViewMessage.setLayoutParams(params);
    }

    private void setupTextMessage(TextMessageViewHolder holder, Message message, boolean isAdmin, String time) {
        holder.textMessage.setText(message.getMessageText());
        holder.textTime.setText(time);

        // Set text color based on sender
        int textColor = ContextCompat.getColor(context, 
            isAdmin ? R.color.message_text_color_admin : R.color.message_text_color_user);
        holder.textMessage.setTextColor(textColor);
        holder.textTime.setTextColor(textColor);

        // Set background color
        holder.cardViewMessage.setCardBackgroundColor(ContextCompat.getColor(context,
            isAdmin ? R.color.admin_message_bg : R.color.user_message_bg));

        // Update layout alignment
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardViewMessage.getLayoutParams();
        if (isAdmin) {
            params.leftMargin = (int) context.getResources().getDimension(R.dimen.message_margin_large);
            params.rightMargin = (int) context.getResources().getDimension(R.dimen.message_margin_small);
        } else {
            params.leftMargin = (int) context.getResources().getDimension(R.dimen.message_margin_small);
            params.rightMargin = (int) context.getResources().getDimension(R.dimen.message_margin_large);
        }
        holder.cardViewMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public void updateData(List<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    static class TextMessageViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewMessage;
        TextView textMessage;
        TextView textTime;

        public TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewMessage = itemView.findViewById(R.id.cardViewMessage);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }

    static class ProductMessageViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewMessage;
        TextView textMessage;
        TextView textTime;
        CardView cardViewProduct;
        ImageView imgProduct;
        TextView txtProductName;
        TextView txtProductPrice;

        public ProductMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewMessage = itemView.findViewById(R.id.cardViewMessage);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
            cardViewProduct = itemView.findViewById(R.id.cardViewProduct);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
        }
    }
}