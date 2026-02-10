package com.example.remarket.domain.item.dto.request;

import lombok.Data;

@Data
public class ItemSearchCondition {

    private String title;
    private Integer minPrice;
    private Integer maxPrice;
    private String sort;

}
