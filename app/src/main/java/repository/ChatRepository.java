package repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import models.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {
    private FirebaseFirestore db;
    private CollectionReference messagesRef;

    public ChatRepository() {
        db = FirebaseFirestore.getInstance();
        messagesRef = db.collection("chats");
    }

    public void sendMessage(String userId, String messageText, OnMessageSentListener listener) {
        Message message = new Message(userId, "admin", messageText, System.currentTimeMillis());
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> listener.onMessageSent(true))
                .addOnFailureListener(e -> listener.onMessageSent(false));
    }

    public void sendAdminMessage(String adminId, String receiverId, String messageText, OnMessageSentListener listener) {
        Message message = new Message("admin", receiverId, messageText, System.currentTimeMillis());
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> listener.onMessageSent(true))
                .addOnFailureListener(e -> listener.onMessageSent(false));
    }

    public void getMessages(String userId, OnMessagesReceivedListener listener) {
        Query query = messagesRef
            .whereEqualTo("senderId", userId)
            .whereEqualTo("receiverId", "admin")
            .orderBy("timestamp", Query.Direction.ASCENDING);

        // Add real-time listener
        return query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                listener.onMessagesReceived(null);
                return;
            }

            if (snapshots != null) {
                List<Message> messages = new ArrayList<>(snapshots.toObjects(Message.class));
                listener.onMessagesReceived(messages);
            }
        });
    }

    public void getAdminMessages(String userId, OnMessagesReceivedListener listener) {
        Query userToAdminQuery = messagesRef
            .whereEqualTo("senderId", userId)
            .whereEqualTo("receiverId", "admin");

        Query adminToUserQuery = messagesRef
            .whereEqualTo("senderId", "admin")
            .whereEqualTo("receiverId", userId);

        // Add real-time listener for both queries
        userToAdminQuery.addSnapshotListener((userToAdminSnapshots, error1) -> {
            if (error1 != null) {
                listener.onMessagesReceived(null);
                return;
            }

            adminToUserQuery.addSnapshotListener((adminToUserSnapshots, error2) -> {
                if (error2 != null) {
                    listener.onMessagesReceived(null);
                    return;
                }

                List<Message> allMessages = new ArrayList<>();
                
                if (userToAdminSnapshots != null) {
                    allMessages.addAll(userToAdminSnapshots.toObjects(Message.class));
                }
                
                if (adminToUserSnapshots != null) {
                    allMessages.addAll(adminToUserSnapshots.toObjects(Message.class));
                }
                
                allMessages.sort((msg1, msg2) -> Long.compare(msg1.getTimestamp(), msg2.getTimestamp()));
                listener.onMessagesReceived(allMessages);
            });
        });
    }

    public interface OnMessageSentListener {
        void onMessageSent(boolean success);
    }

    public interface OnMessagesReceivedListener {
        void onMessagesReceived(List<Message> messages);
    }
}
