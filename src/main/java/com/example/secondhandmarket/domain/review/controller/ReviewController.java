package com.example.secondhandmarket.domain.review.controller;

import com.example.secondhandmarket.domain.review.dto.request.ReviewCreateRequest;
import com.example.secondhandmarket.domain.review.dto.response.ReviewResponse;
import com.example.secondhandmarket.domain.review.service.ReviewService;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 후기 작성 API
     */
    @PostMapping
    public ResponseEntity<Void> createReview(@AuthenticationPrincipal AuthMember authMember,
                                             @RequestBody ReviewCreateRequest request) {
        reviewService.createReview(authMember.getMemberId(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 내가 받은 후기 목록 조회 API
     */
    @GetMapping("/received")
    public ResponseEntity<Slice<ReviewResponse>> getReceivedReviews(@AuthenticationPrincipal AuthMember authMember,
                                                                    @PageableDefault(size = 20) Pageable pageable) {
        Slice<ReviewResponse> responses = reviewService.getReceivedReviews(authMember.getMemberId(), pageable);
        return ResponseEntity.ok(responses);
    }
}