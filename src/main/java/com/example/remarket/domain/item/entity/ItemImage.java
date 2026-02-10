package com.example.remarket.domain.item.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "item_images")
@Getter
public class ItemImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_representative")
    private Boolean isRepresentative;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // 생성자
    protected ItemImage() { }

    private ItemImage(String imageUrl, Boolean isRepresentative, Integer sortOrder) {
        this.imageUrl = imageUrl;
        this.isRepresentative = isRepresentative;
        this.sortOrder = sortOrder;
    }

    public static ItemImage createItemImage(String imageUrl, Boolean isRepresentative, Integer sortOrder) {
        return new ItemImage(imageUrl, isRepresentative, sortOrder);
    }

    // 연관관계 편의 메서드
    public void setItem(Item item) {
        this.item = item;
    }

    // 비즈니스 로직
}
