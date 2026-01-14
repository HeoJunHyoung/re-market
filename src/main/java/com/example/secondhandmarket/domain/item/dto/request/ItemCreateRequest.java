package com.example.secondhandmarket.domain.item.dto.request;

import com.example.secondhandmarket.domain.item.entity.enumerate.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemCreateRequest {

    private String title;
    private String content;
    private Integer price;
    private String tradePlace;
    private Category category;

}
