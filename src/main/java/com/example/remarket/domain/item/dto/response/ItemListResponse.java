package com.example.remarket.domain.item.dto.response;

import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.item.entity.ItemImage;
import com.example.remarket.domain.item.entity.enumerate.ItemStatus;
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
    private Long tradeId;
    private String title;
    private String tradePlace;
    private Integer price;
    private String thumbnailUrl; // 대표 이미지 URL
    private ItemStatus status;
    private Integer chatCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;

    public static ItemListResponse fromEntity(Item item, Long tradeId) {

        String thumbnailUrl = item.getItemImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsRepresentative()))
                .findFirst()
                .map(ItemImage::getImageUrl)
                .orElse(null);

        return ItemListResponse.builder()
                .itemId(item.getId())
                .tradeId(tradeId)
                .title(item.getTitle())
                .tradePlace(item.getTradePlace())
                .price(item.getPrice())
                .thumbnailUrl(thumbnailUrl)
                .status(item.getStatus())
                .chatCount(item.getChatCount())
                .favoriteCount(item.getFavoriteCount())
                .createdAt(item.getCreatedAt())
                .build();
    }

}
