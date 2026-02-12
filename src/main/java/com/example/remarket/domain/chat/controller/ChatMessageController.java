package com.example.remarket.domain.chat.controller;

import com.example.remarket.global.security.principal.AuthMember;
import com.example.remarket.domain.chat.dto.request.ChatMessageRequest;
import com.example.remarket.domain.chat.dto.response.ChatMessageResponse;
import com.example.remarket.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    // WebSocket 메시지 전송
    @MessageMapping("/chat/message/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageRequest request, Principal principal) {

        // 1. Principal -> AuthMember 변환
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        AuthMember authMember = (AuthMember) auth.getPrincipal();

        log.info("채팅 전송 - 방: {}, 발신자: {}, 내용: {}", roomId, authMember.getMemberId(), request.getMessage());

        // 2. 서비스 호출
        chatService.sendMessage(roomId, authMember.getMemberId(), request);
    }

    // 채팅 내역 조회 (HTTP)
    @GetMapping("/chat/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getChatHistory(roomId));
    }
}