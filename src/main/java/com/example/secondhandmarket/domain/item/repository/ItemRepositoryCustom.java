package com.example.secondhandmarket.domain.item.repository;

import com.example.secondhandmarket.domain.item.dto.request.ItemSearchCondition;
import com.example.secondhandmarket.domain.item.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ItemRepositoryCustom {
    Slice<Item> searchItems(ItemSearchCondition condition, Pageable pageable);
}
