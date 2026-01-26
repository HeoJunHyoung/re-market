package com.example.secondhandmarket.concurrency;

import com.example.secondhandmarket.config.IntegrationTest;
import com.example.secondhandmarket.domain.auth.service.AuthService;
import com.example.secondhandmarket.domain.chat.repository.jpa.ChatRoomRepository;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.enumerate.Category;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemType;
import com.example.secondhandmarket.domain.item.repository.FavoriteRepository;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@ActiveProfiles("test")
public class ConcurrencyProblemTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private TradeRepository tradeRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;

    // 삭제 순서 처리를 위해 추가된 리포지토리
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private FavoriteRepository favoriteRepository;

    @Autowired private ReviewService reviewService;
    @Autowired private ItemService itemService;
    @Autowired private AuthService authService;

    // 매 테스트가 끝날 때마다 데이터를 깨끗하게 지워줍니다.
    // (외래 키 제약 조건 때문에 자식 데이터부터 지워야 합니다.)
    @AfterEach
    void tearDown() {
        reviewRepository.deleteAllInBatch();   // 후기 삭제
        favoriteRepository.deleteAllInBatch(); // 찜 삭제
        chatRoomRepository.deleteAllInBatch(); // 채팅방 삭제
        tradeRepository.deleteAllInBatch();    // 거래 삭제
        itemRepository.deleteAllInBatch();     // 상품 삭제
        memberRepository.deleteAllInBatch();   // 회원 삭제
    }

    @Test
    @DisplayName("[동시성 문제 확인] 1. 낙관적 락 미적용: 10명이 동시에 후기를 남기면 점수가 누락된다.")
    void reviewScore_lost_update_test() throws InterruptedException {
        // given
        // 판매자 생성 (기본 점수 300점)
        Member seller = Member.ofLocal("seller", "pw", "판매자", "01011111111");
        memberRepository.save(seller);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 구매자 10명과 거래 10개 생성
        Long[] tradeIds = new Long[threadCount];
        Long[] buyerIds = new Long[threadCount];

        Item item = Item.createItem(seller, "dummy", "content", 1000, "loc", Category.DIGITAL_DEVICE, null, ItemType.SALE, 1);
        itemRepository.save(item);

        for (int i = 0; i < threadCount; i++) {
            Member buyer = Member.ofLocal("buyer" + i, "pw", "구매자" + i, "010000000" + i);
            memberRepository.save(buyer);
            buyerIds[i] = buyer.getId();

            Trade trade = Trade.completeTrade(item, seller, buyer);
            tradeRepository.save(trade);
            tradeIds[i] = trade.getId();
        }

        // when
        // 동시에 10명이 'SAFE'(+15점) 평가를 남김
        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    ReviewCreateRequest request = new ReviewCreateRequest(tradeIds[idx], Evaluation.SAFE, null, "굿");
                    reviewService.createReview(buyerIds[idx], request);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Member updatedSeller = memberRepository.findById(seller.getId()).orElseThrow();
        System.out.println("=================================================");
        System.out.println(">> 예상 점수 (최대 일일 제한 적용 전): 300 + (15 * 10) = 450");
        System.out.println(">> 실제 저장된 점수: " + updatedSeller.getSafetyScore());
        System.out.println(">> 설명: 동시성 처리가 안 되어 A스레드가 점수를 읽고 수정해서 저장하기 전에 B스레드가 동일한 점수를 읽어서 덮어씌움.");
        System.out.println("=================================================");

        // 동시성 문제가 있다면 300점에서 거의 오르지 않음 (예: 315)
        assertThat(updatedSeller.getDailyIncrease()).isLessThan(25);
    }

    @Test
    @DisplayName("[동시성 문제 확인] 2. 원자적 업데이트 미적용: 100명이 동시에 좋아요를 누르면 카운트가 100개보다 적다.")
    void favorite_count_lost_update_test() throws InterruptedException {
        // given
        Member seller = Member.ofLocal("seller2", "pw", "판매자2", "01022222222");
        memberRepository.save(seller);
        Item item = Item.createItem(seller, "인기템", "내용", 1000, "서울", Category.DIGITAL_DEVICE, null, ItemType.SALE, 1);
        itemRepository.save(item);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 스레드 풀 제한
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 유저 미리 생성
        Long[] userIds = new Long[threadCount];
        for (int i = 0; i < threadCount; i++) {
            Member user = Member.ofLocal("user" + i, "pw", "유저" + i, "0103333" + (1000 + i));
            memberRepository.save(user);
            userIds[i] = user.getId();
        }

        // when
        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    itemService.toggleFavorite(userIds[idx], item.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        System.out.println("=================================================");
        System.out.println(">> 기대 좋아요 수: 100");
        System.out.println(">> 실제 좋아요 수: " + updatedItem.getFavoriteCount());
        System.out.println(">> 설명: JPA Dirty Checking으로 업데이트 시, 메모리상 값을 읽고 +1 하므로 갱신 손실 발생.");
        System.out.println("=================================================");

        assertThat(updatedItem.getFavoriteCount()).isLessThan(100);
    }

    @Test
    @DisplayName("[동시성 문제 확인] 3. 비관적 락 미적용: 선착순 10명 나눔에 100명이 몰리면 재고 관리가 실패한다.")
    void inventory_concurrency_failure_simulation() throws InterruptedException {
        // 1. Given: 재고 10개인 나눔 상품 준비
        Member owner = Member.ofLocal("owner", "pw", "나눔이", "01011111111");
        memberRepository.save(owner);

        Item sharingItem = Item.createItem(owner, "인기나눔", "내용", 0, "서울", Category.DIGITAL_DEVICE, null, ItemType.SHARING, 10);
        itemRepository.save(sharingItem);

        // 2. When: 100명이 동시에 신청 시도
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 성공한 사람 수 카운트
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // [동시성 문제가 있는 로직 시뮬레이션]
                    synchronized (this) {
                        // 주의: 여기서 synchronized는 테스트 내부 변수 보호용일 뿐, DB 동시성을 막진 못함.
                    }
                    processUnsafeSharingRequest(sharingItem.getId(), successCount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 3. Then: 결과 확인
        Item resultItem = itemRepository.findById(sharingItem.getId()).orElseThrow();

        System.out.println("=================================================");
        System.out.println(">> 초기 재고: 10개 / 시도 인원: 100명");
        System.out.println(">> 기대 성공 인원: 10명 / 실제 성공 인원: " + successCount.get() + "명");
        System.out.println(">> 남은 재고: " + resultItem.getStockQuantity() + "개");
        System.out.println("=================================================");

        if (successCount.get() != 10 || resultItem.getStockQuantity() != 0) {
            System.out.println(">>> 동시성 문제 발생 확인됨! <<<");
        }

        // 실제로 문제가 발생해야 테스트 통과 (역설적)
        assertThat(successCount.get()).isNotEqualTo(10);
    }

    // [불안정한 로직] 락 없이 DB에서 읽고 -> 메모리에서 줄이고 -> 저장하는 방식
    private void processUnsafeSharingRequest(Long itemId, AtomicInteger successCount) {
        // 1. 조회
        Item item = itemRepository.findById(itemId).orElseThrow();

        // 2. 재고 확인
        if (item.getStockQuantity() > 0) {
            // 3. 재고 감소 (메모리 상에서 수행)
            int currentStock = item.getStockQuantity();
            ReflectionTestUtils.setField(item, "stockQuantity", currentStock - 1);

            // 4. 저장 (여기서 다른 스레드가 수정한 내용을 덮어씌움 -> 갱신 손실 발생)
            itemRepository.save(item);

            successCount.incrementAndGet();
        }
    }
}