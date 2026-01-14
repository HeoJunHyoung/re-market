package com.example.secondhandmarket.domain.item.repository;

import com.example.secondhandmarket.domain.item.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
        SELECT i FROM Item i ORDER BY i.createdAt DESC
    """)
    Slice<Item> findLatestItems(Pageable pageable);

}
