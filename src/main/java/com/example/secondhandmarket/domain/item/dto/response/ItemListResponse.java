package com.example.secondhandmarket.domain.item.dto.response;

import com.example.secondhandmarket.domain.item.entity.enumerate.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemListResponse {

    private Long itemId;
    private String title;
    private String tradePlace;
    private Integer price;
    private String thumbnailUrl; // 대표 이미지 URL
    private ItemStatus status;
    private Integer chatCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;

}
