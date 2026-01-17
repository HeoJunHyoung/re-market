package com.example.secondhandmarket.domain.item.repository;

import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
        SELECT i FROM Item i ORDER BY i.createdAt DESC
    """)
    Slice<Item> findLatestItems(Pageable pageable);

    // 판매 상태(status)에 따른 나의 판매 내역 조회
    @Query("""
        SELECT i 
        FROM Item i
        WHERE i.member.id = :memberId AND (:status IS NULL OR i.status = :status) ORDER BY i.createdAt DESC
    """)
    Slice<Item> findAllByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") ItemStatus status, Pageable pageable);}
