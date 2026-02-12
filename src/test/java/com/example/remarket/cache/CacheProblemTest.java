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
                itemService.getItemDetails(invalidItemId, null);
            } catch (Exception e) {
                // 예외 무시
            }
        }

        // then
        verify(itemRepository, times(requestCount)).findById(invalidItemId);
        System.out.println(">> [검증 실패 의도] 10번 요청에 대해 DB 조회 10회 발생 확인");
    }

    @Test
    @DisplayName("[Thundering Herd] 메인 페이지 캐시 만료 직후, 동시 요청이 오면 모두 DB를 조회한다.")
    void mainPageThunderingHerdTest() throws InterruptedException {
        // given
        redisTemplate.delete("main:items:page:0"); // 캐시 강제 삭제

        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 3번째 파라미터에 null (비로그인 멤버) 전달 -> 글로벌 메인 페이지 조회
                    itemService.getItemList(new ItemSearchCondition(), PageRequest.of(0, 20), null);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // searchItems 파라미터가 3개(Condition, List, Pageable)로 늘어났으므로 any() 3개 필요
        verify(itemRepository, atLeast(2)).searchItems(any(), any(), any());
        System.out.println(">> [검증 실패 의도] 동시 요청 시 DB 조회가 1회로 방어되지 않고 다수 발생함");
    }
}