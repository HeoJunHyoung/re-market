package com.example.secondhandmarket.domain.review.entity;

import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.review.entity.enumerate.Evaluation;
import com.example.secondhandmarket.domain.review.entity.enumerate.ReviewTag;
import com.example.secondhandmarket.domain.trade.entity.Trade;
import com.example.secondhandmarket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Member reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private Member reviewee;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation", nullable = false)
    private Evaluation evaluation; // SAFE, NORMAL, UNSAFE

    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private ReviewTag tag; // 선택 안 함 가능 (nullable)

    @Column(name = "content")
    private String content;

    private Review(Trade trade, Member reviewer, Member reviewee, Evaluation evaluation, ReviewTag tag, String content) {
        this.trade = trade;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.evaluation = evaluation;
        this.tag = tag;
        this.content = content;
    }

    public static Review createReview(Trade trade, Member reviewer, Member reviewee, Evaluation evaluation, ReviewTag tag, String content) {
        return new Review(trade, reviewer, reviewee, evaluation, tag, content);
    }
}