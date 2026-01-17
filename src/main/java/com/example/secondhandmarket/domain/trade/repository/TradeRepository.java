package com.example.secondhandmarket.domain.trade.repository;

import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.trade.entity.Trade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    // 판매 내역
    List<Trade> findBySellerId(Long sellerId);

    // 구매 내역
    List<Trade> findByBuyerId(Long buyerId);

    // 구매 내역 페이징 조회 (최신순)
    Slice<Trade> findAllByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    long countBySellerAndBuyer(Member seller, Member buyer);
}
