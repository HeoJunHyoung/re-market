package com.example.secondhandmarket.domain.item.dto.response;

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

}
