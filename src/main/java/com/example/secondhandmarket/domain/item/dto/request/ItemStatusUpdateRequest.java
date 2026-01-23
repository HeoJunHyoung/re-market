package com.example.secondhandmarket.domain.item.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemStatusUpdateRequest {
    private String status; // "ON_SALE", "RESERVED", "SOLD"
    private Long buyerId;  // 거래 완료(SOLD)일 때 필수
}