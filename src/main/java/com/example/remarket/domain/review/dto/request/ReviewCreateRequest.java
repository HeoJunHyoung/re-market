package com.example.remarket.domain.review.dto.request;

import com.example.remarket.domain.review.entity.enumerate.ReviewTag;
import com.example.remarket.domain.review.entity.enumerate.Evaluation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateRequest {
    private Long tradeId;
    private Evaluation evaluation; // SAFE, NORMAL, UNSAFE
    private ReviewTag tag;         // 선택된 단일 태그 (없을 수 있음)
    private String content;        // 후기 내용
}
