package com.example.remarket.concurrency;

import com.example.remarket.config.IntegrationTest;
import com.example.remarket.domain.chat.repository.jpa.ChatRoomRepository;
import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.item.entity.enumerate.Category;
import com.example.remarket.domain.item.entity.enumerate.ItemType;
import com.example.remarket.domain.item.repository.FavoriteRepository;
import com.example.remarket.domain.item.repository.ItemRepository;
import com.example.remarket.domain.item.service.ItemService;
import com.example.remarket.domain.member.entity.Member;
import com.example.remarket.domain.member.repository.MemberRepository;
import com.example.remarket.domain.review.dto.request.ReviewCreateRequest;
import com.example.remarket.domain.review.entity.enumerate.Evaluation;
import com.example.remarket.domain.review.repository.ReviewRepository;
import com.example.remarket.domain.review.service.ReviewService;
import com.example.remarket.domain.trade.entity.Trade;
import com.example.remarket.domain.trade.repository.TradeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@ActiveProfiles("test")
public class ConcurrencySuccessTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private TradeRepository tradeRepository;
    @Autowired private ReviewService reviewService;
    @Autowired private ItemService itemService;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAllInBatch();
        favoriteRepository.deleteAllInBatch();
        chatRoomRepository.deleteAllInBatch();
        tradeRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[해결 검증] 1. 낙관적 락: 충돌 시 예외가 발생하며, 재시도 시 모든 점수가 정상 반영된다.")
    void review_optimistic_lock_success_test() throws InterruptedException {
        // given
        Member seller = Member.ofLocal("seller", "pw", "판매자", "01011111111");
        memberRepository.save(seller);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

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
        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    ReviewCreateRequest request = new ReviewCreateRequest(tradeIds[idx], Evaluation.SAFE, null, "굿");

                    // [재시도 로직 시뮬레이션]
                    // 낙관적 락은 충돌 시 예외를 던지므로, 클라이언트나 서비스가 재시도를 해야 성공함
                    while (true) {
                        try {
                            reviewService.createReview(buyerIds[idx], request);
                            break; // 성공하면 탈출
                        } catch (Exception e) {
                            // 버전 충돌 발생 시 50ms 대기 후 재시도
                            Thread.sleep(50);
                        }
                    }
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
        System.out.println(">> 최종 점수: " + updatedSeller.getSafetyScore());
        System.out.println(">> 일일 상승량: " + updatedSeller.getDailyIncrease());

        // 10명이 모두 성공했으므로, 일일 제한폭인 25점까지 꽉 차게 올라야 함 (기존엔 누락돼서 15점 등이었음)
        assertThat(updatedSeller.getDailyIncrease()).isEqualTo(25);
    }

    @Test
    @DisplayName("[해결 검증] 2. 원자적 업데이트: 100명이 동시에 좋아요를 눌러도 정확히 100개가 반영된다.")
    void favorite_atomic_update_success_test() throws InterruptedException {
        // given
        Member seller = Member.ofLocal("seller2", "pw", "판매자2", "01022222222");
        memberRepository.save(seller);
        Item item = Item.createItem(seller, "인기템", "내용", 1000, "서울", Category.DIGITAL_DEVICE, null, ItemType.SALE, 1);
        itemRepository.save(item);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

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
                // [수정] 데드락 발생 시 재시도하도록 while 루프 추가
                while (true) {
                    try {
                        itemService.toggleFavorite(userIds[idx], item.getId());
                        break; // 성공 시 탈출
                    } catch (Exception e) {
                        // 데드락 등 예외 발생 시 잠시 대기 후 재시도
                        try { Thread.sleep(50); } catch (InterruptedException ex) {}
                    }
                }
                latch.countDown();
            });
        }

        latch.await();

        // then
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        System.out.println(">> D 좋아요 수: " + updatedItem.getFavoriteCount());

        // 원자적 업데이트 덕분에 갱신 손실 없이 정확히 100이 되어야 함
        assertThat(updatedItem.getFavoriteCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("[해결 검증] 3. 비관적 락: 선착순 10명 나눔에 100명이 몰려도 재고는 정확히 처리된다.")
    void sharing_pessimistic_lock_success_test() throws InterruptedException {
        // given
        Member owner = Member.ofLocal("owner", "pw", "나눔이", "01011111111");
        memberRepository.save(owner);
        // 재고 10개
        Item sharingItem = Item.createItem(owner, "인기나눔", "내용", 0, "서울", Category.DIGITAL_DEVICE, null, ItemType.SHARING, 10);
        itemRepository.save(sharingItem);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    // 유저별로 다른 ID를 사용해야 중복 신청 체크(채팅방 존재 여부)를 통과하고 재고 경쟁을 함
                    // 테스트 편의상 DB에 유저를 미리 다 안 넣고, 임의의 ID를 파라미터로 넘긴다고 가정 (Mocking 필요할 수도 있으나 통합테스트니 유저 생성 필요)
                    // 여기서는 유저 생성 비용을 줄이기 위해 service 호출부만 테스트한다고 가정하거나, 위 setup에서 유저를 미리 만드세요.
                    // **시간 관계상 여기서는 '재고 감소' 로직만 집중적으로 검증하기 위해 같은 유저가 아니라 '서로 다른 유저'라고 가정하고 루프 안에서 생성**
                    // (주의: 실제로는 @Transactional 밖이라 느릴 수 있음. 미리 만드는 게 정석)

                    Member applicant = Member.ofLocal("app" + idx, "pw", "신청자" + idx, "0109999" + idx);
                    memberRepository.save(applicant);

                    itemService.requestSharing(sharingItem.getId(), applicant.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Item resultItem = itemRepository.findById(sharingItem.getId()).orElseThrow();
        System.out.println(">> 시도: 100명");
        System.out.println(">> 성공: " + successCount.get() + "명");
        System.out.println(">> 실패: " + failCount.get() + "명");
        System.out.println(">> 남은 재고: " + resultItem.getStockQuantity());

        // 검증: 정확히 10명만 성공해야 함
        assertThat(successCount.get()).isEqualTo(10);
        // 검증: 재고는 0이어야 함 (마이너스가 되거나 남으면 안됨)
        assertThat(resultItem.getStockQuantity()).isEqualTo(0);
    }
}