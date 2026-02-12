package com.example.remarket.domain.item.dto.request;

import com.example.remarket.domain.item.entity.enumerate.ItemStatus;
import lombok.Data;

@Data
public class ItemSearchCondition {

    private String title;
    private Integer minPrice;
    private Integer maxPrice;
    private String sort;
    private ItemStatus status;
    private Integer distanceLevel;

}
