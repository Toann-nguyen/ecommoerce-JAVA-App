package adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages;
    private Context context;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textMessage.setText(message.getMessageText());

        // Format the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(message.getTimestamp()));
        holder.textTime.setText(formattedTime);

        // Check if the message is from current user
        boolean isCurrentUser = message.getSenderId().equals(currentUserId);

        // Set message appearance based on sender
        if (isCurrentUser) {
            // User's message
            holder.cardViewMessage.setCardBackgroundColor(ContextCompat.getColor(context, R.color.purple_500));
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.cardViewMessage.getLayoutParams();
            params.horizontalBias = 1.0f; // Align to right
            holder.cardViewMessage.setLayoutParams(params);
        } else {
            // Admin's message
            holder.cardViewMessage.setCardBackgroundColor(ContextCompat.getColor(context, R.color.purple_200));
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.cardViewMessage.getLayoutParams();
            params.horizontalBias = 0.0f; // Align to left
            holder.cardViewMessage.setLayoutParams(params);
        }
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

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewMessage;
        TextView textMessage;
        TextView textTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewMessage = itemView.findViewById(R.id.cardViewMessage);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }
}