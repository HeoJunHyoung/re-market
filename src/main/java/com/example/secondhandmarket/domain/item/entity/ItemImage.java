package com.example.secondhandmarket.domain.item.entity;

import jakarta.persistence.*;
import lombok.Generated;
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

    @Column(name = "content")
    private String content;

    @Column(name = "is_representative")
    private Boolean isRepresentative;

    @Column(name = "sort_order")
    private Integer sortOrder;

    protected ItemImage() { }
}
