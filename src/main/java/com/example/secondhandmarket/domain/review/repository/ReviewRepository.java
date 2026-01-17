package com.example.secondhandmarket.domain.review.repository;

import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.review.entity.Review;
import com.example.secondhandmarket.domain.trade.entity.Trade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 해당 거래에 대해 내가 이미 쓴 후기가 있는지 확인 (중복 방지)
    Optional<Review> findByTradeAndReviewerId(Trade trade, Long reviewerId);

    // [마이페이지용] 내가 받은 후기 목록 조회 (최신순)
    @Query("""
        SELECT r
        FROM Review r
        JOIN FETCH r.reviewer
        WHERE r.reviewee.id = :memberId
        ORDER BY r.createdAt DESC
    """)
    Slice<Review> findALlByRevieweeId(@Param("memberId") Long memberId, Pageable pageable);

}
