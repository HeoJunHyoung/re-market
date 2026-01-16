package com.example.secondhandmarket.domain.chat.controller;

import com.example.secondhandmarket.domain.chat.dto.response.ChatRoomResponse;
import com.example.secondhandmarket.domain.chat.service.ChatService;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * 채팅방 생성 또는 기존 방 조회
     */
    @PostMapping
    public ResponseEntity<Long> createChatRoom(@AuthenticationPrincipal AuthMember authMember,
                                               @RequestParam Long itemId) {
        Long chatRoomId = chatService.createOrGetChatRoom(itemId, authMember.getMemberId());
        return ResponseEntity.ok(chatRoomId);
    }

    /**
     * 내 채팅방 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(@AuthenticationPrincipal AuthMember authMember) {
        return ResponseEntity.ok(chatService.getMyChatRooms(authMember.getMemberId()));
    }
}