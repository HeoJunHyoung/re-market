package com.example.remarket.domain.review.entity.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewTag {
    // SAFE Tags (+2점)
    TIME_PUNCTUAL("시간 약속을 잘 지켜요", 2),
    FAST_RESPONSE("응답이 빨라요", 2),
    DETAILED_EXPLANATION("설명이 정확해요", 2),
    GOOD_MANNER("매너가 좋아요", 2),

    // UNSAFE Tags (-5점)
    PROMISE_BROKEN("약속 불이행", -5),
    NO_RESPONSE("응답 없음", -5),
    GHOSTING("거래 중 잠수", -5),
    BAD_ATTITUDE("태도 불량", -5),
    EXCESSIVE_BARGAINING("가격 흥정 과도", -5);

    private final String description;
    private final int score;
}
