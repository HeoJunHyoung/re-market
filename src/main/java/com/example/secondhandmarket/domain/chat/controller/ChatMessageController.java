package com.example.secondhandmarket.domain.chat.controller;

import com.example.secondhandmarket.domain.chat.dto.request.ChatMessageRequest;
import com.example.secondhandmarket.domain.chat.dto.response.ChatMessageResponse;
import com.example.secondhandmarket.domain.chat.service.ChatService;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    // WebSocket 메시지 전송
    @MessageMapping("/chat/message/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageRequest request,
                            @AuthenticationPrincipal AuthMember authMember) {
        chatService.sendMessage(roomId, authMember.getMemberId(), request);
    }

    // 채팅 내역 조회 (HTTP)
    @GetMapping("/chat/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getChatHistory(roomId));
    }
}