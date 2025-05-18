package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import models.User;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> users;
    private final OnUserClickListener listener;
    private int selectedPosition = -1;

    public UserChatAdapter(Context context, List<User> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.textUserName.setText(user.getFullName());

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(holder.imageUser);
        } else {
            holder.imageUser.setImageResource(R.drawable.profile_placeholder);
        }

        // Highlight selected user
        if (position == selectedPosition) {
            holder.cardUser.setCardBackgroundColor(ContextCompat.getColor(context, R.color.purple_200));
            holder.textUserName.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.cardUser.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.textUserName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }

            notifyItemChanged(selectedPosition);
            listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CardView cardUser;
        ShapeableImageView imageUser;
        TextView textUserName;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUser = itemView.findViewById(R.id.cardUser);
            imageUser = itemView.findViewById(R.id.imageUser);
            textUserName = itemView.findViewById(R.id.textUserName);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }
}