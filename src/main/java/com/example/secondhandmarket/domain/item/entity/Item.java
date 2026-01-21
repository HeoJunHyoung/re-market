package com.example.secondhandmarket.domain.item.entity;

import com.example.secondhandmarket.domain.item.entity.enumerate.Category;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemStatus;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items", indexes = {
        // 1. 카테고리 + 최신순 정렬용 복합 인덱스
        @Index(name = "idx_items_category_created_at", columnList = "category, created_at DESC"),

        // 2. 판매상태 + 최신순 정렬용 복합 인덱스
        @Index(name = "idx_items_status_created_at", columnList = "status, created_at DESC"),

        // 3. 가격 검색용 단일 인덱스
        @Index(name = "idx_items_price", columnList = "price"),

        // 4. 전체 최신순 정렬용 인덱스
        @Index(name = "idx_items_created_at", columnList = "created_at DESC")
})
@Getter
public class Item extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member member; // 판매자

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "chat_count")
    private Integer chatCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "trade_place")
    private String tradePlace;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> itemImages = new ArrayList<>();

    // 생성자
    protected Item() { }

    private Item(Member member, String title, String content, Integer price, String tradePlace, Category category) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.price = price;
        this.tradePlace = tradePlace;
        this.category = category;
        this.status = ItemStatus.ON_SALE;
    }

    public static Item createItem(Member member, String title, String content, Integer price,
                                  String tradePlace, Category category, List<ItemImage> itemImages) {
        Item item = new Item(member, title, content, price, tradePlace, category);

        if (itemImages != null) {
            itemImages.forEach(item::addItemImage);
        }
        return item;
    }

    // 연관관계 편의메서드
    public void addItemImage(ItemImage image) {
        this.itemImages.add(image);
        image.setItem(this);
    }

    // 비즈니스 로직

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseFavoriteCount() {
        this.favoriteCount ++;
    }

    public void decreaseFavoriteCount() {
        this.favoriteCount --;
    }

    public void changeStatus(ItemStatus status) {
        this.status = status;
    }

}