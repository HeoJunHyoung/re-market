package com.example.secondhandmarket.domain.trade.repository;

import com.example.secondhandmarket.domain.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    // 판매 내역
    List<Trade> findBySellerId(Long sellerId);

    // 구매 내역
    List<Trade> findByBuyerId(Long buyerId);

}
