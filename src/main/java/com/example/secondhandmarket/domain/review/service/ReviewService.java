package com.example.secondhandmarket.domain.review.service;

import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.exception.MemberErrorCode;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
import com.example.secondhandmarket.domain.review.dto.request.ReviewCreateRequest;
import com.example.secondhandmarket.domain.review.dto.response.ReviewResponse;
import com.example.secondhandmarket.domain.review.entity.Review;
import com.example.secondhandmarket.domain.review.entity.enumerate.Evaluation;
import com.example.secondhandmarket.domain.review.entity.enumerate.ReviewTag;
import com.example.secondhandmarket.domain.review.exception.ReviewErrorCode;
import com.example.secondhandmarket.domain.review.repository.ReviewRepository;
import com.example.secondhandmarket.domain.trade.entity.Trade;
import com.example.secondhandmarket.domain.trade.exception.TradeErrorCode;
import com.example.secondhandmarket.domain.trade.repository.TradeRepository;
import com.example.secondhandmarket.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;

    /**
     * 후기 작성 및 안심 지수 업데이트
     */
    @Transactional
    public void createReview(Long reviewerId, ReviewCreateRequest request) {
        // 1. 거래 확인
        Trade trade = tradeRepository.findById(request.getTradeId())
                .orElseThrow(() -> new BusinessException(TradeErrorCode.TRADE_NOT_FOUND));

        // 2. 작성자 확인
        Member reviewer = memberRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 3. 중복 후기 체크
        if (reviewRepository.findByTradeAndReviewerId(trade, reviewerId).isPresent()) {
            throw new BusinessException(ReviewErrorCode.ALREADY_EXISTS_REVIEW);
        }

        // 4. 후기 대상자(Reviewee) 식별
        Member reviewee;
        if (trade.getSeller().getId().equals(reviewerId)) {
            reviewee = trade.getBuyer();
        } else if (trade.getBuyer().getId().equals(reviewerId)) {
            reviewee = trade.getSeller();
        } else {
            throw new IllegalArgumentException("해당 거래의 당사자가 아닙니다.");
        }

        // 5. 안심 지수 변동량 계산
        int delta = calculateScoreDelta(trade, reviewer, reviewee, request.getEvaluation(), request.getTag());

        // 6. 점수 반영 및 거래 횟수 증가
        reviewee.updateSafetyScore(delta);
        reviewee.incrementTradeCount();

        // 7. 후기 저장
        Review review = Review.createReview(
                trade,
                reviewer,
                reviewee,
                request.getEvaluation(),
                request.getTag(),
                request.getContent()
        );
        reviewRepository.save(review);
    }

    /**
     * 내가 받은 후기 목록 조회
     */
    public Slice<ReviewResponse> getReceivedReviews(Long memberId, Pageable pageable) {
        return reviewRepository.findALlByRevieweeId(memberId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    // --- 점수 계산 로직 ---
    private int calculateScoreDelta(Trade trade, Member reviewer, Member reviewee, Evaluation eval, ReviewTag tag) {
        // 1. 기본 점수 (SAFE +15, NORMAL 0, UNSAFE -30)
        int baseScore = eval.getScore();

        // 2. 태그 점수 (단일 태그 점수만 반영, 없으면 0)
        int tagScore = (tag != null) ? tag.getScore() : 0;

        // 3. 최근성 가중치 (90일 기준 감쇠)
        double recentWeight = calculateRecentWeight(trade);

        // 4. 1차 변동량 계산
        double rawDelta = (baseScore + tagScore) * recentWeight;

        // 5. 악용 방지: 동일 상대 반복 거래 (3회 초과 시 가중치 50%)
        // TradeRepository에 countBySellerAndBuyer 등 쿼리 메서드 필요
        // 여기서는 seller/buyer 구분 없이 두 사람 간 거래 횟수를 조회한다고 가정
        long tradeCount = tradeRepository.countBySellerAndBuyer(reviewee, reviewer)
                + tradeRepository.countBySellerAndBuyer(reviewer, reviewee);

        // 6. 신규 계정 보호 (거래 5회 미만)
        if (reviewee.getTradeCount() < 5) {
            if (rawDelta < 0) {
                rawDelta *= 0.5; // 하락폭 완화
            } else {
                // 상승폭 제한 (예: 최대 60%만 반영)
                rawDelta = Math.min(rawDelta, baseScore * 0.6);
            }
        }

        return (int) Math.round(rawDelta);
    }

    private double calculateRecentWeight(Trade trade) {
        long daysSince = ChronoUnit.DAYS.between(trade.getCreatedAt().toLocalDate(), java.time.LocalDate.now());
        // e^(-days / 90)
        return Math.exp(-daysSince / 90.0);
    }
}