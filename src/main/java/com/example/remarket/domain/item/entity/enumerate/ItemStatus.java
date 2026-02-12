package com.example.remarket.domain.item.entity.enumerate;

import lombok.Getter;

@Getter
public enum ItemStatus {
    ON_SALE("판매중"),
    RESERVED("예약중"),
    SOLD("판매완료");

    private String description;

    ItemStatus(String description) {
        this.description = description;
    }
}
