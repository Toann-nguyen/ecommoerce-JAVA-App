package com.example.ecommerce.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.Review;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {

    private List<Review> reviews;
    private Context context;
    private SimpleDateFormat dateFormat;
    private ReviewActionListener listener;

    public interface ReviewActionListener {
        void onDeleteReview(Review review);
    }

    public AdminReviewAdapter(List<Review> reviews, Context context, ReviewActionListener listener) {
        this.reviews = reviews;
        this.context = context;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void updateData(List<Review> newReviews) {
        this.reviews = newReviews;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvUserName, tvDate, tvComment;
        RatingBar ratingBar;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvComment = itemView.findViewById(R.id.tvComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Review review) {
            // Display product image
            if (review.getProductImage() != null && !review.getProductImage().isEmpty()) {
                Glide.with(context)
                        .load(review.getProductImage())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imgProduct);
            }

            // Display product name
            tvProductName.setText(review.getProductName());

            // Display user name
            tvUserName.setText("Người dùng: " + (review.getUserName() != null ? review.getUserName() : "Ẩn danh"));

            // Display date
            if (review.getCreatedAt() != null) {
                // Convert Timestamp to Date
                Date date = review.getCreatedAt().toDate();
                // Format date as relative time span if less than a week, otherwise use date format
                long now = System.currentTimeMillis();
                long reviewTime = date.getTime();
                if (now - reviewTime < DateUtils.WEEK_IN_MILLIS) {
                    // Less than a week, use relative time
                    CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                            reviewTime, now, DateUtils.MINUTE_IN_MILLIS);
                    tvDate.setText(relativeTime);
                } else {
                    // More than a week, use date format
                    tvDate.setText(dateFormat.format(date));
                }
            } else {
                tvDate.setText("");
            }

            // Display rating
            ratingBar.setRating(review.getRating());

            // Display comment
            tvComment.setText(review.getComment());

            // Setup delete button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteReview(review);
                }
            });
        }
    }
}
