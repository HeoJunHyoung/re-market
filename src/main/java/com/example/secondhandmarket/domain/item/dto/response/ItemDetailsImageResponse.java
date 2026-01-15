package com.example.secondhandmarket.domain.item.dto.response;

import com.example.secondhandmarket.domain.item.entity.ItemImage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDetailsImageResponse {

    private Long itemImageId;
    private Long itemId;
    private String imageUrl;
    private Boolean isRepresentative;
    private Integer sortOrder;

    public static ItemDetailsImageResponse fromEntity(ItemImage itemImage) {
        return ItemDetailsImageResponse.builder()
                .itemImageId(itemImage.getId())
                .itemId(itemImage.getItem().getId())
                .imageUrl(itemImage.getImageUrl())
                .isRepresentative(itemImage.getIsRepresentative())
                .sortOrder(itemImage.getSortOrder())
                .build();
    }

}
