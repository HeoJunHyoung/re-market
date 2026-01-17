package com.example.secondhandmarket.domain.review.entity.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SafetyLevel {
    BEST("최상", 850, 1000),
    VERY_TRUSTED("매우 신뢰", 700, 849),
    TRUSTED("신뢰", 500, 699),
    GOOD("준수", 400, 499),
    NORMAL("일반", 300, 399),
    NOT_TRUSTED("신뢰 부족", 200, 299),
    CAUTION("거래 주의", 0, 199);

    private final String label;
    private final int minScore;
    private final int maxScore;

    public static SafetyLevel of(int score) {
        return Arrays.stream(values())
                .filter(level -> score >= level.minScore && score <= level.maxScore)
                .findFirst()
                .orElse(NORMAL); // 예외 시 기본값
    }
}
