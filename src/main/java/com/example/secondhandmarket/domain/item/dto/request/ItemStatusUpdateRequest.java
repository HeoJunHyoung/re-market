package com.example.secondhandmarket.domain.item.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemStatusUpdateRequest {
    private String status; // "ON_SALE", "RESERVED", "SOLD_OUT"
    private Long buyerId;  // 거래 완료(SOLD_OUT)일 때 필수
}