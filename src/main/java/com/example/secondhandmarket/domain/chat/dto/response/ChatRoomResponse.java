package com.example.secondhandmarket.domain.chat.dto.response;

import com.example.secondhandmarket.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long chatRoomId;
    private Long itemId;
    private String itemTitle;
    private Long sellerId;
    private Long buyerId;

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .itemId(chatRoom.getItem().getId())
                .itemTitle(chatRoom.getItem().getTitle())
                .sellerId(chatRoom.getSeller().getId())
                .buyerId(chatRoom.getBuyer().getId())
                .build();
    }
}