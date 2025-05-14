package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import models.User;
import models.UserRole;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private OnUserItemClickListener listener;

    public interface OnUserItemClickListener {
        void onUserClick(User user);

        void onEditRoleClick(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<User> newUserList) {
        this.userList = newUserList;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView userCard;
        private ImageView imgUserAvatar;
        private TextView txtUserName, txtUserEmail, txtUserRole, txtPermissionCount;
        private ImageButton btnEditRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userCard = itemView.findViewById(R.id.userCard);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            txtUserRole = itemView.findViewById(R.id.txtUserRole);
            txtPermissionCount = itemView.findViewById(R.id.txtPermissionCount);
            btnEditRole = itemView.findViewById(R.id.btnEditRole);
        }

        void bind(final User user) {
            // Set user name
            String fullName = user.getFullName();
            if (fullName == null || fullName.isEmpty()) {
                fullName = "Người dùng";
            }
            txtUserName.setText(fullName);

            // Set user email
            txtUserEmail.setText(user.getEmail());

            // Set user avatar
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(imgUserAvatar);
            } else {
                imgUserAvatar.setImageResource(R.drawable.ic_profile);
            }

            // Set role with appropriate color
            setupRoleLabel(txtUserRole, user.getRole());

            // Set permissions count
            int permissionsCount = user.getPermissions() != null ? user.getPermissions().size() : 0;
            txtPermissionCount.setText(permissionsCount + " quyền");

            // Set click listeners
            userCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });

            btnEditRole.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditRoleClick(user);
                }
            });
        }

        private void setupRoleLabel(TextView textView, String role) {
            String roleText;
            int roleColor;

            if (UserRole.ADMIN.getRole().equals(role)) {
                roleText = "Admin";
                roleColor = Color.parseColor("#F44336"); // Red
            } else if (UserRole.SELLER.getRole().equals(role)) {
                roleText = "Người bán";
                roleColor = Color.parseColor("#2196F3"); // Blue
            } else {
                roleText = "Người dùng";
                roleColor = Color.parseColor("#4CAF50"); // Green
            }

            textView.setText(roleText);
            textView.getBackground().setTint(roleColor);
        }
    }
}