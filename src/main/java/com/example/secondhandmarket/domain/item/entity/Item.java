package com.example.secondhandmarket.domain.item.entity;

import com.example.secondhandmarket.domain.item.entity.enumerate.Category;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemStatus;
import com.example.secondhandmarket.domain.member.entity.Address;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "items")
@Getter
public class Item extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member member;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "price")
    private Integer price;

    @Column(name = "wish_count")
    private Integer wishCount;

    @Column(name = "chat_count")
    private Integer chatCount;

    @Column(name = "trade_place")
    private String tradePlace;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Category category;

    protected Item() { }


}
