package com.example.remarket.domain.item.dto.request;

import com.example.remarket.domain.item.entity.enumerate.Category;
import lombok.Data;
import java.util.List;

@Data
public class ItemUpdateRequest {
    private String title;
    private String content;
    private Integer price;
    private String tradePlace;
    private Category category;

    // 삭제할 기존 이미지들의 ID 목록
    private List<Long> deletedImageIds;
}