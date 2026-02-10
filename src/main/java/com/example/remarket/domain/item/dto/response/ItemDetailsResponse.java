package com.example.remarket.domain.item.dto.response;

import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.item.entity.enumerate.ItemStatus;
import com.example.remarket.domain.member.entity.Address;
import com.example.remarket.domain.item.entity.enumerate.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailsResponse {

    private Long itemId;

    private Long sellerId; // 판매자 Id (AUTO INCR)
    private String sellerNickname; // 판매자 이름
    private Address sellerAddress; // 판매자 동네
    private Integer sellerSafetyScore; // 판매자 안심 지수

    private String title;
    private String content;
    private Integer price;
    private Integer favoriteCount;
    private Integer chatCount;
    private Integer viewCount;
    private String tradePlace;

    private ItemStatus status;
    private Category category;

    private Boolean isFavorite;

    @Builder.Default
    private List<ItemDetailsImageResponse> itemImages = new ArrayList<>();

    public static ItemDetailsResponse fromEntity(Item item, boolean isFavorite) {

        List<ItemDetailsImageResponse> imageResponses = item.getItemImages().stream()
                .map(ItemDetailsImageResponse::fromEntity)
                .toList();

        return ItemDetailsResponse.builder()
                .itemId(item.getId())
                .sellerId(item.getMember().getId())
                .sellerNickname(item.getMember().getNickname())
                .sellerAddress(item.getMember().getAddress())
                .sellerSafetyScore(item.getMember().getSafetyScore())
                .title(item.getTitle())
                .content(item.getContent())
                .price(item.getPrice())
                .favoriteCount(item.getFavoriteCount())
                .chatCount(item.getChatCount())
                .viewCount(item.getViewCount())
                .tradePlace(item.getTradePlace())
                .status(item.getStatus())
                .category(item.getCategory())
                .itemImages(imageResponses)
                .isFavorite(isFavorite)
                .build();

    }

}
