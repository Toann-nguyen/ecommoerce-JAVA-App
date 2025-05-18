package repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import models.Message;
import models.Product;

public class ChatRepository {
    private final CollectionReference messagesRef;
    private ListenerRegistration messageListener;
    private boolean isProductMessageBeingSent = false;

    public interface OnMessageSentListener {
        void onMessageSent(boolean success);
    }

    public interface OnMessagesReceivedListener {
        void onMessagesReceived(List<Message> messages);
    }

    public ChatRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        messagesRef = db.collection("messages");
    }

    public void sendMessage(String userId, String messageText, OnMessageSentListener listener, OnMessagesReceivedListener messagesListener) {
        Message message = new Message(userId, "admin", messageText, System.currentTimeMillis());
        
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) {
                        listener.onMessageSent(true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onMessageSent(false);
                    }
                });
    }

    public void sendProductMessage(String userId, String messageText, Product product, OnMessageSentListener listener, OnMessagesReceivedListener messagesListener) {
        if (isProductMessageBeingSent) {
            return; // Prevent duplicate sends
        }
        
        isProductMessageBeingSent = true;
        Message message = new Message(userId, "admin", messageText, product, System.currentTimeMillis());
        
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    isProductMessageBeingSent = false;
                    if (listener != null) {
                        listener.onMessageSent(true);
                    }
                })
                .addOnFailureListener(e -> {
                    isProductMessageBeingSent = false;
                    if (listener != null) {
                        listener.onMessageSent(false);
                    }
                });    }

    public void sendAdminMessage(String adminId, String userId, String messageText, OnMessageSentListener listener, OnMessagesReceivedListener messagesListener) {
        Message message = new Message("admin", userId, messageText, System.currentTimeMillis());
        
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    if (messagesListener != null) {
                        List<Message> currentMessages = new ArrayList<>();
                        currentMessages.add(message);
                        messagesListener.onMessagesReceived(currentMessages);
                    }
                    if (listener != null) {
                        listener.onMessageSent(true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onMessageSent(false);
                    }
                });
    }

    public void getMessages(String userId, OnMessagesReceivedListener listener) {
        // Cancel any existing listener
        if (messageListener != null) {
            messageListener.remove();
        }

        // Create new listener
        messageListener = messagesRef
            .whereIn("senderId", Arrays.asList(userId, "admin"))
            .whereIn("receiverId", Arrays.asList(userId, "admin"))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    listener.onMessagesReceived(null);
                    return;
                }

                List<Message> messages = new ArrayList<>();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Message message = doc.toObject(Message.class);
                        messages.add(message);
                    }
                }
                listener.onMessagesReceived(messages);
            });
    }

    public List<ListenerRegistration> getAdminMessages(String userId, OnMessagesReceivedListener listener) {
        List<ListenerRegistration> registrations = new ArrayList<>();

        // Sử dụng một query duy nhất với điều kiện OR ngầm định
        Query combinedQuery = messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .whereIn("senderId", Arrays.asList(userId, "admin"))
            .whereIn("receiverId", Arrays.asList(userId, "admin"));

        ListenerRegistration registration = combinedQuery.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                listener.onMessagesReceived(null);
                return;
            }

            if (snapshots != null && !snapshots.isEmpty()) {
                List<Message> messages = snapshots.toObjects(Message.class);
                // Lọc để chỉ lấy tin nhắn liên quan đến user hiện tại và admin
                List<Message> filteredMessages = messages.stream()
                    .filter(msg -> (msg.getSenderId().equals(userId) && msg.getReceiverId().equals("admin")) ||
                                 (msg.getSenderId().equals("admin") && msg.getReceiverId().equals(userId)))
                    .collect(Collectors.toList());
                listener.onMessagesReceived(filteredMessages);
            } else {
                listener.onMessagesReceived(new ArrayList<>());
            }
        });

        registrations.add(registration);
        return registrations;
    }

    public void getUsersWithChats(OnUsersWithChatsListener listener) {
        messagesRef
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Set<String> userIds = new HashSet<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Message message = doc.toObject(Message.class);
                    if (!"admin".equals(message.getSenderId())) {
                        userIds.add(message.getSenderId());
                    }
                    if (!"admin".equals(message.getReceiverId())) {
                        userIds.add(message.getReceiverId());
                    }
                }
                listener.onUsersWithChatsReceived(new ArrayList<>(userIds));
            })
            .addOnFailureListener(e -> listener.onUsersWithChatsReceived(new ArrayList<>()));
    }

    public interface OnUsersWithChatsListener {
        void onUsersWithChatsReceived(List<String> userIds);
    }

    public void cleanup() {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
    }
}
