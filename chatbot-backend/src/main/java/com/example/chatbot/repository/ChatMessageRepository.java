package com.example.chatbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chatbot.model.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserIdOrderByTimestampDesc(String userId);

    List<ChatMessage> findByUserIdAndSessionIdOrderByTimestampAsc(String userId, String sessionId);
}