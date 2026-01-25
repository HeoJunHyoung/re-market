package com.example.secondhandmarket.domain.chat.entity;

import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "chat_rooms")
@Getter
public class ChatRoom extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Member buyer;

    // 생성자
    protected ChatRoom() { }

    private ChatRoom(Item item, Member seller, Member buyer) {
        this.item = item;
        this.seller = seller;
        this.buyer = buyer;
    }

    public static ChatRoom createChatRoom(Item item, Member buyer) {
        return new ChatRoom(item, item.getMember(), buyer);
    }

    // 연관관계 편의 메서드


    // 비즈니스 로직


}
