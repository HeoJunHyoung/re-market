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

    @MockitoSpyBean
    private ItemRepository itemRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("[Penetration 방어] 존재하지 않는 ID를 반복 조회해도, DB 조회는 최초 1회만 발생해야 한다.")
    void cachePenetrationSuccessTest() {
        // given
        long invalidItemId = 99999999L;

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
        redisTemplate.delete("main:items:page:0");

        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 3번째 파라미터 null 전달 (비로그인 -> 글로벌 캐시 로직 진입)
                    itemService.getItemList(new ItemSearchCondition(), PageRequest.of(0, 20), null);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // searchItems 파라미터가 3개이므로 any() 3개로 검증
        verify(itemRepository, times(1)).searchItems(any(), any(), any());

        System.out.println("==================================================");
        System.out.println("[Thundering Herd 방지 성공]");
        System.out.println("동시 요청자 수: " + threadCount);
        System.out.println("DB 조회 횟수: 1 (분산 락과 DCL이 정상 작동함)");
        System.out.println("==================================================");
    }
}