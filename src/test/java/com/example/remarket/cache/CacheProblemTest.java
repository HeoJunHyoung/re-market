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
public class CacheProblemTest {

    @Autowired private ItemService itemService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    // 실제 DB 조회가 일어나는지 감시하기 위해 SpyBean 사용
    @MockitoSpyBean
    private ItemRepository itemRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("[Cache Penetration] 존재하지 않는 ID 조회 시 캐싱되지 않아 매번 DB를 조회한다.")
    void cachePenetrationTest() {
        // given
        long invalidItemId = 99999999L; // DB에 없는 ID

        // when
        int requestCount = 10;
        for (int i = 0; i < requestCount; i++) {
            try {
                // 현재 로직: 캐시 확인(없음) -> DB 조회(없음) -> 예외 발생
                // 문제점: '없음'이라는 사실이 캐싱되지 않으므로 계속 DB를 찌름
                itemService.getItemDetails(invalidItemId, null);
            } catch (Exception e) {
                // 예외 무시
            }
        }

        // then
        // 방어 로직이 없다면 요청 횟수만큼 DB 조회가 발생함
        verify(itemRepository, times(requestCount)).findById(invalidItemId);
        System.out.println(">> [검증 실패 의도] 10번 요청에 대해 DB 조회 10회 발생 확인");
    }

    @Test
    @DisplayName("[Thundering Herd] 메인 페이지 캐시 만료 직후, 동시 요청이 오면 모두 DB를 조회한다.")
    void mainPageThunderingHerdTest() throws InterruptedException {
        // given
        redisTemplate.delete("main:items:page:0"); // 캐시 강제 삭제 (만료 상황)

        int threadCount = 30; // 30명이 동시에 접속
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    itemService.getItemList(new ItemSearchCondition(), PageRequest.of(0, 20));
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // 동기화(Double Checked Locking)가 없다면 DB 조회가 1회 이상(거의 30회) 발생
        verify(itemRepository, atLeast(2)).searchItems(any(), any());
        System.out.println(">> [검증 실패 의도] 동시 요청 시 DB 조회가 1회로 방어되지 않고 다수 발생함");
    }
}