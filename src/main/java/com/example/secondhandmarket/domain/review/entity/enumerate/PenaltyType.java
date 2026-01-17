package com.example.secondhandmarket.domain.review.entity.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PenaltyType {
    NO_SHOW(-50),       // 노쇼
    BLOCK_IN_TRADE(-25),// 거래 중 차단
    REPORT_VERIFIED(-40), // 신고 누적 (최소치)
    REPEAT_CANCEL(-20); // 반복 거래 취소

    private final int score;
}