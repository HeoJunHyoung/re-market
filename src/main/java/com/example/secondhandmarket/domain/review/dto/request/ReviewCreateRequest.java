package com.example.secondhandmarket.domain.review.dto.request;

import com.example.secondhandmarket.domain.review.entity.enumerate.Evaluation;
import com.example.secondhandmarket.domain.review.entity.enumerate.ReviewTag;
import lombok.Data;


@Data
public class ReviewCreateRequest {
    private Long tradeId;
    private Evaluation evaluation; // SAFE, NORMAL, UNSAFE
    private ReviewTag tag;         // 선택된 단일 태그 (없을 수 있음)
    private String content;        // 후기 내용
}
