package com.example.secondhandmarket.domain.chat.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "chat_message")
@Getter
public class ChatMessage {

    @Id
    private String id;

    private Long chatRoomId; // MySQL ChatRoomId

    private Long senderId;

    private String message;

    private LocalDateTime createdAt;

    protected ChatMessage() { }

    @Builder
    public ChatMessage(Long chatRoomId, Long senderId, String message) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

}
