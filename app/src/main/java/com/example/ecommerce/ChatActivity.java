package com.example.ecommerce;

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
import repository.ChatRepository;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerMessages;
    private EditText editTextMessage;
    private MaterialButton buttonSend;
    private MaterialToolbar chatToolbar;

    private ChatRepository chatRepository;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase and get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRepository = new ChatRepository();

        // Initialize UI components
        recyclerMessages = findViewById(R.id.recyclerMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        chatToolbar = findViewById(R.id.chatToolbar);

        // Set up toolbar
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(messageAdapter);

        // Load messages with real-time updates
        setupMessageListener();

        // Set up send button
        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
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

    private void setupMessageListener() {
        chatRepository.getMessages(currentUserId, messages -> {
            if (messages != null) {
                messageList.clear();
                messageList.addAll(messages);
                messageAdapter.notifyDataSetChanged();
                scrollToBottom();
            } else {
                Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String messageText) {
        chatRepository.sendMessage(currentUserId, messageText, new ChatRepository.OnMessageSentListener() {
            @Override
            public void onMessageSent(boolean success) {
                if (success) {
                    editTextMessage.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            recyclerMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }
}