package com.example.remarket.domain.chat.dto.response;

import com.example.remarket.domain.chat.entity.ChatRoom;
import com.example.remarket.domain.member.entity.Member;
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
    private String partnerName;

    public static ChatRoomResponse from(ChatRoom chatRoom, Long myMemberId) {
        Member partner = chatRoom.getSeller().getId().equals(myMemberId)
                ? chatRoom.getBuyer()
                : chatRoom.getSeller();

        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .itemId(chatRoom.getItem().getId())
                .itemTitle(chatRoom.getItem().getTitle())
                .sellerId(chatRoom.getSeller().getId())
                .buyerId(chatRoom.getBuyer().getId())
                .partnerName(partner.getNickname())
                .build();
    }
}