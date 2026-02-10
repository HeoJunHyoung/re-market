package com.example.remarket.domain.item.repository;

import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.item.dto.request.ItemSearchCondition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ItemRepositoryCustom {
    Slice<Item> searchItems(ItemSearchCondition condition, Pageable pageable);
}
