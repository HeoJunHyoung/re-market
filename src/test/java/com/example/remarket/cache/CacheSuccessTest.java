package com.example.remarket.cache;

import com.example.remarket.config.IntegrationTest;
import com.example.remarket.domain.item.dto.request.ItemSearchCondition;
import com.example.remarket.domain.item.repository.ItemRepository;
import com.example.remarket.domain.item.service.ItemService;
import com.example.remarket.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@IntegrationTest
public class CacheSuccessTest {

    @Autowired private ItemService itemService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    // 실제 DB 조회가 몇 번 일어나는지 감시(Spy)
    @MockitoSpyBean
    private ItemRepository itemRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        // 테스트 간 독립성을 위해 Redis 데이터 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("[Penetration 방어] 존재하지 않는 ID를 반복 조회해도, DB 조회는 최초 1회만 발생해야 한다.")
    void cachePenetrationSuccessTest() {
        // given
        long invalidItemId = 99999999L; // DB에 없는 ID

        // when
        int requestCount = 10;
        for (int i = 0; i < requestCount; i++) {
            try {
                // 1번째 호출: DB 조회 후 "__NULL__" 캐싱
                // 2~10번째 호출: 캐시의 "__NULL__"을 보고 바로 예외 발생 (DB 조회 X)
                itemService.getItemDetails(invalidItemId, null);
            } catch (Exception e) {
                // ITEM_NOT_FOUND 예외는 정상적인 동작이므로 무시하고 진행
            }
        }

        // then
        // [검증 성공 기준] 요청은 10번 했지만, DB 조회 메서드(findById)는 딱 1번만 호출되어야 함
        verify(itemRepository, times(1)).findById(invalidItemId);

        System.out.println("==================================================");
        System.out.println("[Cache Penetration 방어 성공]");
        System.out.println("요청 횟수: " + requestCount);
        System.out.println("DB 조회 횟수: 1 (나머지 9번은 Redis에서 차단됨)");
        System.out.println("==================================================");
    }

    @Test
    @DisplayName("[Thundering Herd 방지] 캐시 만료 직후 30명 동시 요청 시, DB 조회는 단 1회만 발생해야 한다.")
    void thunderingHerdSuccessTest() throws InterruptedException {
        // given
        // 캐시가 없는 상태 (Cache Miss 유발)
        redisTemplate.delete("main:items:page:0");

        int threadCount = 30; // 30명이 동시에 접속한다고 가정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 메인 페이지 조회 (분산 락 적용됨)
                    itemService.getItemList(new ItemSearchCondition(), PageRequest.of(0, 20));
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); // 모든 스레드가 끝날 때까지 대기

        // then
        // [검증 성공 기준]
        // 1. 첫 번째 스레드가 Lock 획득 -> DB 조회 -> 캐시 저장
        // 2. 나머지 스레드는 Lock 대기 -> Lock 획득 후 캐시 조회(DCL) -> 반환
        // 결론: DB 조회 메서드(searchItems)는 단 1번만 실행되어야 함.
        verify(itemRepository, times(1)).searchItems(any(), any());

        System.out.println("==================================================");
        System.out.println("[Thundering Herd 방지 성공]");
        System.out.println("동시 요청자 수: " + threadCount);
        System.out.println("DB 조회 횟수: 1 (분산 락과 DCL이 정상 작동함)");
        System.out.println("==================================================");
    }
}