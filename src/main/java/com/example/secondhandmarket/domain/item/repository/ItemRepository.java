package com.example.secondhandmarket.domain.item.repository;

import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

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
    Slice<Item> findAllByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") ItemStatus status, Pageable pageable);


    /**
     * [나눔 전용] 재고 감소 (원자적 업데이트)
     * - 재고가 0보다 클 때만 감소시킴 (stockQuantity > 0 조건 필수)
     * - 반환값: 업데이트된 행의 개수 (1이면 성공, 0이면 재고 없음)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Item i SET i.stockQuantity = i.stockQuantity - 1 WHERE i.id = :itemId AND i.stockQuantity > 0")
    int decreaseStockAtomic(@Param("itemId") Long itemId);
}
