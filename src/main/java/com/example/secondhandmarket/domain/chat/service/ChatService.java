package com.example.secondhandmarket.domain.chat.service;

import com.example.secondhandmarket.domain.chat.dto.request.ChatMessageRequest;
import com.example.secondhandmarket.domain.chat.dto.response.ChatMessageResponse;
import com.example.secondhandmarket.domain.chat.dto.response.ChatRoomResponse;
import com.example.secondhandmarket.domain.chat.entity.ChatMessage;
import com.example.secondhandmarket.domain.chat.entity.ChatRoom;
import com.example.secondhandmarket.domain.chat.repository.jpa.ChatRoomRepository;
import com.example.secondhandmarket.domain.chat.repository.mongo.ChatMessageRepository;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.repository.ItemRepository;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅방 생성 또는 기존 방 조회 (MySQL - Transactional 필요)
     */
    @Transactional
    public Long createOrGetChatRoom(Long itemId, Long buyerId) {
        Item item = itemRepository.getReferenceById(itemId);
        Member buyer = memberRepository.getReferenceById(buyerId);

        return chatRoomRepository.findByItemAndBuyer(item, buyer)
                .map(ChatRoom::getId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.createChatRoom(item, buyer);
                    chatRoomRepository.save(newRoom);
                    return newRoom.getId();
                });
    }

    /**
     * 내 채팅방 목록 조회 (MySQL - ReadOnly)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyChatRooms(Long memberId) {
        return chatRoomRepository.findMyChatRooms(memberId).stream()
                .map(ChatRoomResponse::from)
                .toList();
    }

    /**
     * 메시지 전송 (MongoDB 저장 & WebSocket 전송)
     * MongoDB는 JPA Transactional을 타지 않으므로 어노테이션 불필요 (필요 시 MongoTransactionManager 설정 해야함)
     */
    public void sendMessage(Long roomId, Long senderId, ChatMessageRequest request) {
        // 1. MongoDB 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoomId(roomId)
                .senderId(senderId)
                .message(request.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);

        // 2. WebSocket 전송
        ChatMessageResponse response = ChatMessageResponse.from(chatMessage);
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, response);
    }

    /**
     * 채팅 내역 조회 (MongoDB)
     */
    public List<ChatMessageResponse> getChatHistory(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }
}