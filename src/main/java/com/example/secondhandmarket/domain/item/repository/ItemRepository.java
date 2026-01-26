package com.example.secondhandmarket.domain.item.repository;

import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    // 관심 수 증가 (원자적 업데이트)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Item i SET i.favoriteCount = i.favoriteCount + 1 WHERE i.id = :id")
    void increaseFavoriteCount(@Param("id") Long id);

    // 관심 수 감소 (원자적 업데이트)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Item i SET i.favoriteCount = i.favoriteCount - 1 WHERE i.id = :id")
    void decreaseFavoriteCount(@Param("id") Long id);

    // 선착순 나눔 신청 목적: 비관적 락을 건 상태로 아이템 조회
    // ㄴ 선착순 신청 시에는 조회 시점부터 다른 스레드가 건들지 못하게 막음
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Optional<Item> findByIdWithPessimisticLock(@Param("id") Long id);
}
