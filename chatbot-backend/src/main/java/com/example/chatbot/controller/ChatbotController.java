package com.example.chatbot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatbot.model.ChatMessage;
import com.example.chatbot.model.FAQ;
import com.example.chatbot.service.ChatbotService;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    /**
     * Process user message and return bot response
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> processMessage(@RequestBody Map<String, String> request) {
        String userId = request.getOrDefault("userId", "anonymous");
        String message = request.get("message");

        if (message == null || message.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Message cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ChatMessage response = chatbotService.processMessage(userId, message);

        Map<String, String> apiResponse = new HashMap<>();
        apiResponse.put("response", response.getMessage());
        apiResponse.put("timestamp", response.getTimestamp().toString());

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get chat history for a specific user
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String userId) {
        List<ChatMessage> history = chatbotService.getChatHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get frequently asked questions
     */
    @GetMapping("/faq")
    public ResponseEntity<List<FAQ>> getFAQs() {
        List<FAQ> faqs = new ArrayList<>();

        // Thêm câu hỏi thường gặp
        faqs.add(new FAQ("Làm thế nào để đặt hàng?",
                "Bạn có thể dễ dàng đặt hàng bằng cách thêm sản phẩm vào giỏ hàng và tiến hành thanh toán."));

        faqs.add(new FAQ("Cửa hàng có chấp nhận thanh toán qua thẻ tín dụng không?",
                "Có, chúng tôi chấp nhận tất cả các loại thẻ tín dụng và debit card chính."));

        faqs.add(new FAQ("Thời gian giao hàng là bao lâu?",
                "Thời gian giao hàng thông thường từ 3-5 ngày làm việc tùy thuộc vào địa điểm của bạn."));

        faqs.add(new FAQ("Chính sách đổi trả hàng như thế nào?",
                "Bạn có thể đổi trả hàng trong vòng 7 ngày kể từ ngày nhận hàng nếu sản phẩm còn nguyên vẹn."));

        faqs.add(new FAQ("Làm thế nào để theo dõi đơn hàng?",
                "Bạn có thể theo dõi đơn hàng của mình trong mục 'Đơn hàng của tôi' sau khi đăng nhập."));

        return ResponseEntity.ok(faqs);
    }
}