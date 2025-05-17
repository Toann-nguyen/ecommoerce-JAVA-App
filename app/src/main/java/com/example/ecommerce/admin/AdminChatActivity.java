package com.example.ecommerce.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.example.ecommerce.ProductDetailActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import adapters.MessageAdapter;
import adapters.UserChatAdapter;
import models.Message;
import models.Product;
import models.User;
import repository.ChatRepository;
import repository.UserRepository;

public class AdminChatActivity extends AppCompatActivity implements UserChatAdapter.OnUserClickListener, MessageAdapter.OnProductClickListener {

    private RecyclerView recyclerUsers;
    private RecyclerView recyclerMessages;
    private EditText editTextMessage;
    private MaterialButton buttonSend;
    private MaterialToolbar chatToolbar;

    private ChatRepository chatRepository;
    private UserRepository userRepository;
    private MessageAdapter messageAdapter;
    private UserChatAdapter userAdapter;
    private List<Message> messageList;
    private List<User> userList;

    private String selectedUserId;
    private String adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        // Initialize Firebase and get admin ID
        adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRepository = new ChatRepository();
        userRepository = new UserRepository();

        // Initialize UI components
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        chatToolbar = findViewById(R.id.chatToolbar);

        // Set up toolbar
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Chat với người dùng");

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, this);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(messageAdapter);

        // Initialize user list and adapter
        userList = new ArrayList<>();
        userAdapter = new UserChatAdapter(this, userList, this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerUsers.setAdapter(userAdapter);

        // Load users with chat history
        loadUsersWithChats();

        // Set up send button
        buttonSend.setOnClickListener(v -> {
            if (selectedUserId != null) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage();
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn người dùng để chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUsersWithChats() {
        FirebaseFirestore.getInstance().collection("chats")
                .whereEqualTo("receiverId", "admin")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Lỗi khi tải danh sách người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Extract unique user IDs
                    List<String> userIds = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (Message message : queryDocumentSnapshots.toObjects(Message.class)) {
                            if (!userIds.contains(message.getSenderId()) && !message.getSenderId().equals("admin")) {
                                userIds.add(message.getSenderId());
                            }
                        }
                    }

                    // Fetch user details
                    if (!userIds.isEmpty()) {
                        userRepository.getUsersByIds(userIds, new UserRepository.OnUsersLoadedListener() {
                            @Override
                            public void onUsersLoaded(List<User> users) {
                                userList.clear();
                                userList.addAll(users);
                                userAdapter.notifyDataSetChanged();

                                // Auto-select first user if available and no user is selected
                                if (!users.isEmpty() && selectedUserId == null) {
                                    onUserClick(users.get(0));
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(AdminChatActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    private void loadMessages(String userId) {
        chatRepository.getAdminMessages(userId, messages -> {
            if (messages != null) {
                messageList.clear();
                messageList.addAll(messages);
                messageAdapter.notifyDataSetChanged();
                scrollToBottom();
            } else {
                Toast.makeText(AdminChatActivity.this, "Không thể tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Clear the input field
            editTextMessage.setText("");

            chatRepository.sendAdminMessage(
                adminId,
                selectedUserId,
                messageText,
                success -> {
                    if (!success) {
                        // Show error message if sending failed
                        Toast.makeText(AdminChatActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                    }
                },
                messages -> {
                    // Update the messages list
                    messageAdapter.updateData(messages);
                }
            );
        }
    }

    @Override
    public void onUserClick(User user) {
        selectedUserId = user.getUid();  // Sử dụng getUid() thay vì getUserId()
        getSupportActionBar().setTitle("Chat với " + user.getFullName());
        loadMessages(selectedUserId);
    }

    @Override
    public void onProductClick(Product product) {
        // Open product details when clicking a product in chat
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            recyclerMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
}