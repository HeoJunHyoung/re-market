package com.example.secondhandmarket.domain.chat.repository;

import com.example.secondhandmarket.domain.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 채팅방 ID로 메시지 내역 조회 (시간순)
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
