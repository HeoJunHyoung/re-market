package com.example.remarket.domain.review.dto.response;

import com.example.remarket.domain.review.entity.enumerate.ReviewTag;
import com.example.remarket.domain.review.entity.Review;
import com.example.remarket.domain.review.entity.enumerate.Evaluation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long reviewerId;
    private String reviewerNickname;

    private Evaluation evaluation;
    private ReviewTag tag;           // 단일 태그
    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerNickname(review.getReviewer().getNickname()) // Member에 getNickname 가정
                .evaluation(review.getEvaluation())
                .tag(review.getTag())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}