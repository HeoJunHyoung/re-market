package com.example.secondhandmarket;

import com.example.secondhandmarket.config.IntegrationTest;
import com.example.secondhandmarket.domain.item.dto.request.ItemStatusUpdateRequest;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.enumerate.Category;
import com.example.secondhandmarket.domain.item.repository.ItemRepository;
import com.example.secondhandmarket.domain.item.service.ItemService;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
import com.example.secondhandmarket.domain.review.dto.request.ReviewCreateRequest;
import com.example.secondhandmarket.domain.review.entity.enumerate.Evaluation;
import com.example.secondhandmarket.domain.review.repository.ReviewRepository;
import com.example.secondhandmarket.domain.review.service.ReviewService;
import com.example.secondhandmarket.domain.trade.entity.Trade;
import com.example.secondhandmarket.domain.trade.repository.TradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class ConcurrencyProblemTest {

    @Autowired private ItemService itemService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TradeRepository tradeRepository;
    @Autowired private ReviewService reviewService;
    @Autowired private ReviewRepository reviewRepository;

    @Test
    @DisplayName("🔥[문제확인 1] 조회수: 100명이 동시에 조회하면, 갱신 분실이 발생하여 조회수가 100 미만이 된다.")
    void viewCount_lost_update_problem() throws InterruptedException {
        // given
        Member seller = saveMember("seller", "010-1111-1111");
        Item item = saveItem(seller, "조회수 테스트 상품", 0); // 초기 조회수 0
        Long itemId = item.getId();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when: 100명이 동시에 상세 조회 (increaseViewCount 발생)
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    itemService.getItemDetails(itemId, null);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then: 락이 없으므로 조회수는 100이 되지 못하고 유실된다.
        Item findItem = itemRepository.findById(itemId).orElseThrow();
        System.out.println("❌ [조회수] 기대값: 100, 실제값: " + findItem.getViewCount());

        // 문제가 발생했음을 검증 (100보다 작으면 테스트 통과)
        assertThat(findItem.getViewCount()).isLessThan(threadCount);
    }

    @Test
    @DisplayName("🔥[문제확인 2] 거래: 판매자가 실수로 '거래 완료'를 2번 따닥 누르면, 거래 내역(Trade)이 2개 생성된다.")
    void duplicate_trade_problem() throws InterruptedException {
        // given
        Member seller = saveMember("seller2", "010-2222-2222");
        Member buyer = saveMember("buyer2", "010-3333-3333");
        Item item = saveItem(seller, "따닥 거래 상품", 10000);
        Long itemId = item.getId();

        int threadCount = 2; // 따닥
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 같은 구매자로 '거래 완료' 요청
        ItemStatusUpdateRequest request = new ItemStatusUpdateRequest("SOLD", buyer.getId());

        // when: 동시에 2번 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    itemService.updateItemStatus(seller.getId(), itemId, request); //
                } catch (Exception e) {
                    // 동시성 제어가 없으면 에러가 안 나고 둘 다 성공해버림
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then: 하나의 아이템에 대해 거래 내역이 2개 생기면 문제 발생
        long tradeCount = tradeRepository.findAll().stream()
                .filter(t -> t.getItem().getId().equals(itemId))
                .count();

        System.out.println("❌ [중복 거래] 기대값: 1, 실제값: " + tradeCount);

        // 문제가 발생했음을 검증 (1보다 크면 테스트 통과)
        assertThat(tradeCount).isGreaterThan(1);
    }

    @Test
    @DisplayName("🔥[문제확인 3] 안심지수/리뷰: 동시에 리뷰를 작성하면 중복 리뷰가 생성되고, 안심 점수가 중복으로 오른다.")
    void review_concurrency_problem() throws InterruptedException {
        // given
        Member seller = saveMember("seller3", "010-4444-4444");
        Member buyer = saveMember("buyer3", "010-5555-5555");
        Item item = saveItem(seller, "리뷰 테스트 상품", 5000);

        // 먼저 정상적으로 거래 완료 처리 (Trade 생성)
        Trade trade = Trade.completeTrade(item, seller, buyer);
        tradeRepository.save(trade);
        Long tradeId = trade.getId();

        // 초기 점수 확인 (기본값 300점이라고 가정)
        int initialScore = seller.getSafetyScore();

        int threadCount = 2; // 따닥 리뷰 작성
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 리뷰 요청 객체 (좋아요 평가)
        ReviewCreateRequest request = new ReviewCreateRequest(tradeId, Evaluation.SAFE, null, "좋아요!");

        // when: 구매자가 판매자에 대해 동시에 2번 리뷰 작성 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reviewService.createReview(buyer.getId(), request);
                } catch (Exception e) {
                    // 동시성 제어가 없으면 중복 체크를 뚫고 둘 다 성공함
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // 1. 리뷰가 2개 생겼는지 확인
        long reviewCount = reviewRepository.findAll().stream()
                .filter(r -> r.getTrade().getId().equals(tradeId))
                .count();
        System.out.println("❌ [중복 리뷰] 기대값: 1, 실제값: " + reviewCount);
        assertThat(reviewCount).isGreaterThan(1); // 2개면 문제 발생

        // 2. 점수가 2번 올랐는지 확인 (GOOD은 점수가 오름)
        Member updatedSeller = memberRepository.findById(seller.getId()).orElseThrow();
        System.out.println("❌ [안심 지수] 초기값: " + initialScore + ", 현재값: " + updatedSeller.getSafetyScore());

        // 점수가 한 번만 올라야 하는데, 초기값과 차이가 1회분 점수보다 크면 중복 반영된 것
        // (정확한 점수 로직을 몰라도, 최소한 변동폭이 2배가 되었을 것임)
        assertThat(updatedSeller.getSafetyScore()).isNotEqualTo(initialScore + Evaluation.SAFE.getScore());
    }

    // --- Helper Methods ---
    private Member saveMember(String name, String phone) {
        return memberRepository.save(Member.ofLocal(name, "pw", name, phone));
    }

    private Item saveItem(Member seller, String title, int price) {
        return itemRepository.save(Item.createItem(seller, title, "content", price, "Seoul", Category.DIGITAL_DEVICE, null));
    }
}