package com.example.ecommerce;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import adapters.MessageAdapter;
import models.Message;
import models.Product;
import repository.ChatRepository;
import repository.FirebaseRepository;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.OnProductClickListener {
    private RecyclerView recyclerMessages;
    private EditText editTextMessage;
    private MaterialButton buttonSend;
    private MaterialToolbar chatToolbar;

    private ChatRepository chatRepository;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String currentUserId;
    private Product selectedProduct;
    private boolean isProductMessageSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components first
        initializeViews();

        // Get current user ID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để chat với admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();
        
        // Initialize chat repository
        chatRepository = new ChatRepository();

        // Setup message listener
        setupMessageListener();

        // Handle product data if available
        handleProductData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void handleProductData() {
        if (isProductMessageSent) return; // Prevent duplicate sends

        if (getIntent().hasExtra("PRODUCT_DATA")) {
            selectedProduct = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");
            String defaultMessage = getIntent().getStringExtra("DEFAULT_MESSAGE");
            if (defaultMessage != null) {
                editTextMessage.setText(defaultMessage);
            }
            // Send product message
            sendProductMessageToAdmin();
        } else if (getIntent().hasExtra("PRODUCT_ID")) {
            // If we only have product ID, load the product first
            String productId = getIntent().getStringExtra("PRODUCT_ID");
            FirebaseRepository repository = new FirebaseRepository();
            repository.getProductById(productId, new FirebaseRepository.ProductCallback() {
                @Override
                public void onCallback(Product loadedProduct) {
                    if (loadedProduct != null) {
                        selectedProduct = loadedProduct;
                        sendProductMessageToAdmin();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(ChatActivity.this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupMessageListener() {
        // Initialize message list and adapter if needed
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        
        if (messageAdapter == null) {
            messageAdapter = new MessageAdapter(this, messageList, this);
            recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
            recyclerMessages.setAdapter(messageAdapter);
        }

        // Single realtime listener for messages
        chatRepository.getMessages(currentUserId, messages -> {
            if (messages != null) {
                messageList.clear();
                messageList.addAll(messages);
                messageAdapter.notifyDataSetChanged();
                scrollToBottom();
            }
        });
    }

    private void sendMessage(String messageText) {
        if (messageText.trim().isEmpty()) return;
        
        chatRepository.sendMessage(currentUserId, messageText,
            success -> {
                if (success) {
                    editTextMessage.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }, null  // No need for messagesListener here since we have realtime updates
        );
    }

    private void sendProductMessageToAdmin() {
        if (selectedProduct != null && !isProductMessageSent) {
            String messageText = editTextMessage.getText().toString().trim();
            chatRepository.sendProductMessage(
                currentUserId, 
                messageText,
                selectedProduct,
                success -> {
                    if (success) {
                        isProductMessageSent = true; // Mark as sent
                        editTextMessage.setText(""); // Clear input after successful send
                    } else {
                        Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                    }
                },
                messages -> {
                    if (messages != null && !messages.isEmpty()) {
                        messageList.add(messages.get(0));
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        scrollToBottom();
                    }
                }
            );
        }
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            recyclerMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onProductClick(Product product) {
        // Chuyển đến trang chi tiết sản phẩm khi click vào sản phẩm trong chat
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }

    private void initializeViews() {
        recyclerMessages = findViewById(R.id.recyclerMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        chatToolbar = findViewById(R.id.chatToolbar);

        // Set up toolbar
        setSupportActionBar(chatToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chat với Admin");
        }

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, this);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(messageAdapter);

        // Set up send button
        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });
    }
}