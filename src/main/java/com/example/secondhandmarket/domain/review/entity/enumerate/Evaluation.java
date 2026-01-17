package com.example.secondhandmarket.domain.review.entity.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Evaluation {
    SAFE(15),
    NORMAL(0),
    UNSAFE(-30);

    private final int score;
}