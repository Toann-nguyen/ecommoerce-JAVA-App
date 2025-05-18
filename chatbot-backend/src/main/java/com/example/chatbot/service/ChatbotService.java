package com.example.chatbot.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chatbot.model.ChatMessage;
import com.example.chatbot.repository.ChatMessageRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatbotService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // Simple predefined responses
    private static final Map<String, String> PREDEFINED_RESPONSES = new HashMap<>();

    static {
        PREDEFINED_RESPONSES.put("xin chào", "Xin chào! Tôi có thể giúp gì cho bạn?");
        PREDEFINED_RESPONSES.put("hello", "Xin chào! Tôi có thể giúp gì cho bạn?");
        PREDEFINED_RESPONSES.put("hi", "Chào bạn! Tôi là trợ lý ảo hỗ trợ khách hàng.");
        PREDEFINED_RESPONSES.put("trợ giúp", "Tôi có thể giúp bạn tìm sản phẩm, kiểm tra đơn hàng, hoặc giải đáp thắc mắc.");
        PREDEFINED_RESPONSES.put("sản phẩm", "Chúng tôi cung cấp nhiều loại sản phẩm như điện thoại, laptop, phụ kiện. Bạn muốn tìm sản phẩm nào?");
        PREDEFINED_RESPONSES.put("đơn hàng", "Để kiểm tra đơn hàng, vui lòng cung cấp mã đơn hàng của bạn.");
        PREDEFINED_RESPONSES.put("giá", "Giá sản phẩm được hiển thị chi tiết trên trang sản phẩm. Bạn muốn tìm sản phẩm nào?");
        PREDEFINED_RESPONSES.put("ship", "Chúng tôi giao hàng trong vòng 3-5 ngày làm việc.");
        PREDEFINED_RESPONSES.put("liên hệ", "Bạn có thể liên hệ với chúng tôi qua email support@example.com hoặc hotline 1900-1234.");
        PREDEFINED_RESPONSES.put("cảm ơn", "Rất vui được hỗ trợ bạn! Bạn cần giúp gì nữa không?");
        PREDEFINED_RESPONSES.put("tạm biệt", "Tạm biệt! Rất vui được hỗ trợ bạn.");
    }

    /**
     * Process incoming message and generate a response
     */
    public ChatMessage processMessage(String userId, String message) {
        message = message.trim().toLowerCase();

        // Store user message
        ChatMessage userMessage = saveUserMessage(userId, message);

        // Generate response
        String responseText = generateResponse(message);

        // Save bot response with same session
        ChatMessage botResponse = ChatMessage.builder()
                .userId(userId)
                .message(responseText)
                .isUser(false)
                .timestamp(LocalDateTime.now())
                .sessionId(userMessage.getSessionId())
                .build();

        return chatMessageRepository.save(botResponse);
    }

    /**
     * Save the user message to the database
     */
    private ChatMessage saveUserMessage(String userId, String message) {
        ChatMessage chatMessage = ChatMessage.builder()
                .userId(userId)
                .message(message)
                .isUser(true)
                .timestamp(LocalDateTime.now())
                .sessionId(UUID.randomUUID().toString())
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    /**
     * Generate a response based on user input
     */
    private String generateResponse(String userMessage) {
        // Check for predefined responses
        for (Map.Entry<String, String> entry : PREDEFINED_RESPONSES.entrySet()) {
            if (userMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default response if no match found
        return "Xin lỗi, tôi không hiểu câu hỏi của bạn. Bạn có thể diễn đạt theo cách khác không?";
    }

    /**
     * Get chat history for a user
     */
    public List<ChatMessage> getChatHistory(String userId) {
        return chatMessageRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}