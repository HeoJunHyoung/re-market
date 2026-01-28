package com.example.secondhandmarket.domain.item.service;

import com.example.secondhandmarket.domain.chat.repository.jpa.ChatRoomRepository;
import com.example.secondhandmarket.domain.chat.service.ChatService;
import com.example.secondhandmarket.domain.item.dto.request.ItemCreateRequest;
import com.example.secondhandmarket.domain.item.dto.request.ItemSearchCondition;
import com.example.secondhandmarket.domain.item.dto.request.ItemStatusUpdateRequest;
import com.example.secondhandmarket.domain.item.dto.response.ItemDetailsResponse;
import com.example.secondhandmarket.domain.item.dto.response.ItemListResponse;
import com.example.secondhandmarket.domain.item.entity.Favorite;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.ItemImage;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemStatus;
import com.example.secondhandmarket.domain.item.entity.enumerate.ItemType;
import com.example.secondhandmarket.domain.item.exception.ItemErrorCode;
import com.example.secondhandmarket.domain.item.repository.FavoriteRepository;
import com.example.secondhandmarket.domain.item.repository.ItemRepository;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.exception.MemberErrorCode;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
import com.example.secondhandmarket.domain.trade.entity.Trade;
import com.example.secondhandmarket.domain.trade.repository.TradeRepository;
import com.example.secondhandmarket.global.error.BusinessException;
import com.example.secondhandmarket.global.util.FileUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class ItemService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final TradeRepository tradeRepository;
    private final FileUtil fileUtil;
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    // 캐시 설정 상수
    private static final String MAIN_PAGE_CACHE_KEY = "main:items:page:0";
    private static final long MAIN_PAGE_TTL = 5L; // 메인 페이지 캐시 5초
    // 락 대기 시간을 10초로 설정하여, 먼저 진입한 스레드가 끝날 때까지 충분히 기다리게 함
    private static final long LOCK_WAIT_TIME = 10L;
    private static final long LOCK_LEASE_TIME = 3L;

    // Penetration 방어용 Null Object
    private static final String NULL_OBJECT = "__NULL__";

    /**
     * 상품 등록
     */
    @Transactional
    public void registerItem(Long memberId, ItemCreateRequest request, List<MultipartFile> imageFiles) throws IOException {

        // 1. 판매자 조회
        Member seller = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. ItemImage 객체 리스트 생성
        List<ItemImage> itemImages = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> storedFileNames = fileUtil.storeFiles(imageFiles);
            for (int i = 0; i < storedFileNames.size(); i++) {
                // 첫 번째 이미지를 대표 이미지로 설정
                itemImages.add(ItemImage.createItemImage(storedFileNames.get(i), (i == 0), i));
            }
        }

        int stockQuantity = 1; // 기본값 (판매인 경우)

        if (request.getItemType() == ItemType.SHARING) {
            // 나눔인 경우: 요청받은 수량을 사용 (없거나 0 이하라면 1로 안전하게 처리 or 예외)
            if (request.getStockQuantity() != null && request.getStockQuantity() > 0) {
                stockQuantity = request.getStockQuantity();
            }
        }

        // 3. Item 객체 생성
        Item item = Item.createItem(
                seller,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getTradePlace(),
                request.getCategory(),
                itemImages,
                request.getItemType(),
                stockQuantity
        );

        // 4. 저장
        itemRepository.save(item);

    }

    /**
     * 상품 상태 변경
     */
    @Transactional
    public Long updateItemStatus(Long sellerId, Long itemId, ItemStatusUpdateRequest request) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));

        // 1. 판매자 권한 검증
        if (!item.getMember().getId().equals(sellerId)) {
            throw new BusinessException(ItemErrorCode.NOT_ITEM_OWNER);
        }

        ItemStatus newStatus = ItemStatus.valueOf(request.getStatus());

        // 2. 상태 변경 로직
        if (newStatus == ItemStatus.SOLD) {
            // 거래 완료: 구매자 지정 필수
            if (request.getBuyerId() == null) {
                throw new IllegalArgumentException("구매자 정보가 없습니다.");
            }

            Member seller = memberRepository.findById(sellerId)
                    .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

            Member buyer = memberRepository.findById(request.getBuyerId())
                    .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

            Trade trade = Trade.completeTrade(item, seller, buyer);
            tradeRepository.save(trade);

            item.changeStatus(newStatus);

            // 상태 변경 시 관련 상세 캐시 삭제
            redisTemplate.delete("item:details:" + itemId);

            return trade.getId();

        } else {
            // 판매중 / 예약중: 상태만 변경 (구매자 정보 초기화)
            item.changeStatus(newStatus);
            // 상태 변경 시 관련 상세 캐시 삭제
            redisTemplate.delete("item:details:" + itemId);
            return null;
        }
    }

    /**
     * 상품 수정 (구현 필요 시 추가)
     */

    /**
     * 상품 삭제 (구현 필요 시 추가)
     */

    /**
     * 상품 목록 조회
     * - Cache Aside 전략
     * - Distributed Lock (Thundering Herd 방지)
     * - Double Checked Locking
     */
    public Slice<ItemListResponse> getItemList(ItemSearchCondition condition, Pageable pageable) {
        // 1. 캐싱 대상 여부 확인 (검색 조건 x, 첫 페이지인 경우)
        boolean isMainPage = isMainPageRequest(condition, pageable);

        // 메인 페이지가 아니면 캐시 없이 DB 조회
        if (!isMainPage) {
            return fetchItemListFromDb(condition, pageable);
        }

        // =================================================================
        // [1] 1차 캐시 조회 (Lock 없이 빠르게)
        // =================================================================
        List<ItemListResponse> cachedList = getCachedItemList(MAIN_PAGE_CACHE_KEY);
        if (cachedList != null) {
            log.debug("Cache Hit (1st): Main Page Items");
            return new SliceImpl<>(cachedList, pageable, true);
        }

        // =================================================================
        // [2] Cache Miss -> 분산 락 획득 시도 (Thundering Herd 방지)
        // =================================================================
        String lockKey = MAIN_PAGE_CACHE_KEY + ":lock";
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도 (최대 10초 대기, 3초 점유)
            // *중요*: waitTime을 10초로 넉넉하게 주어, 먼저 락을 잡은 스레드가 작업을 끝낼 때까지 대기하게 함.
            boolean isLocked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);

            if (!isLocked) {
                // 10초를 기다려도 락을 못 잡은 경우 (거의 발생 안 함)
                log.warn("Main Page Lock 획득 실패 (Timeout) - DB 직접 조회");
                return fetchItemListFromDb(condition, pageable);
            }

            // =================================================================
            // [3] Double Checked Locking (DCL)
            // =================================================================
            // 락을 획득하고 진입한 시점에, 앞선 스레드가 이미 캐시를 갱신했을 수 있으므로 다시 확인
            cachedList = getCachedItemList(MAIN_PAGE_CACHE_KEY);
            if (cachedList != null) {
                log.debug("Cache Hit (2nd - DCL): Main Page Items");
                return new SliceImpl<>(cachedList, pageable, true);
            }

            // =================================================================
            // [4] DB 조회 및 캐시 갱신
            // =================================================================
            Slice<ItemListResponse> result = fetchItemListFromDb(condition, pageable);

            // 캐시 저장 (메인 페이지인 경우에만)
            if (result.hasContent()) {
                try {
                    // 전체 Slice 객체 대신 내용물(Content List)만 저장하여 직렬화 이슈 방지
                    redisTemplate.opsForValue().set(MAIN_PAGE_CACHE_KEY, result.getContent(), Duration.ofSeconds(MAIN_PAGE_TTL));
                    log.info("Cache Put: Main Page Items");
                } catch (Exception e) {
                    log.error("Redis Set Failed: {}", e.getMessage());
                }
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Redis Lock Interrupted", e);
        } finally {
            // [5] 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 상품 상세 조회
     * - Cache Penetration 방어 (Null Object Pattern) 적용
     */
    @Transactional
    public ItemDetailsResponse getItemDetails(Long itemId, Long memberId) {

        String cacheKey = "item:details:" + itemId;

        // =================================================================
        // [1] Redis 캐시 조회
        // =================================================================
        Object cachedValue = null;
        try {
            cachedValue = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.error("Redis Get Failed: {}", e.getMessage());
        }

        if (cachedValue != null) {
            // 1-1. [Penetration Hit] "없음"으로 마킹된 데이터인지 확인 (Null Object Pattern)
            if (NULL_OBJECT.equals(cachedValue)) {
                log.debug("Cache Penetration Defense: itemId={}", itemId);
                throw new BusinessException(ItemErrorCode.ITEM_NOT_FOUND);
            }

            // 1-2. [Cache Hit] 정상 데이터 반환
            if (cachedValue instanceof ItemDetailsResponse) {
                // 주의: 캐시 히트 시 조회수 증가 로직 누락됨 (정합성 위해 스킵)
                return (ItemDetailsResponse) cachedValue;
            }
        }

        // =================================================================
        // [2] DB 조회 (Cache Miss)
        // =================================================================
        Item item;
        try {
            item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));
        } catch (BusinessException e) {
            // 3. [Penetration Defense] DB에도 없는 데이터라면 "__NULL__" 캐싱 (30초간 방어)
            if (e.getErrorCode() == ItemErrorCode.ITEM_NOT_FOUND) {
                try {
                    redisTemplate.opsForValue().set(cacheKey, NULL_OBJECT, Duration.ofSeconds(30));
                } catch (Exception redisEx) {
                    log.error("Redis Set Failed (Null Object): {}", redisEx.getMessage());
                }
            }
            throw e;
        }

        // 조회수 증가
        item.increaseViewCount();

        boolean isFavorite = false;
        if (memberId != null) {
            Member member = memberRepository.getReferenceById(memberId);
            isFavorite = favoriteRepository.findByMemberAndItem(member, item).isPresent();
        }

        ItemDetailsResponse response = ItemDetailsResponse.fromEntity(item, isFavorite);

        // =================================================================
        // [4] Redis 정상 데이터 캐싱 (예: 10분)
        // =================================================================
        try {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(10));
        } catch (Exception e) {
            log.error("Redis Set Failed: {}", e.getMessage());
        }

        return response;
    }

    /**
     * 관심 상품 등록/해제 토글
     */
    @Transactional
    public void toggleFavorite(Long memberId, Long itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));

        favoriteRepository.findByMemberAndItem(member, item)
                .ifPresentOrElse(
                        favorite -> {
                            // 이미 있으면 관심 해제
                            favoriteRepository.delete(favorite);
                            entityManager.detach(item);
                            itemRepository.decreaseFavoriteCount(itemId);
                        },
                        () -> {
                            // 없으면 관심 등록
                            Favorite favorite = Favorite.createFavorite(member, item);
                            favoriteRepository.save(favorite);
                            entityManager.detach(item);
                            itemRepository.increaseFavoriteCount(itemId);
                        }
                );
        // 캐시 삭제 (상세 조회 정보 갱신)
        redisTemplate.delete("item:details:" + itemId);
    }

    /**
     * 나의 관심 상품 조회
     */
    public Slice<ItemListResponse> getMyFavoriteItems(Long memberId, Pageable pageable) {
        return favoriteRepository.findMyFavoriteItems(memberId, pageable)
                .map(item -> ItemListResponse.fromEntity(item, null));
    }

    /**
     * 나의 판매 내역 조회
     */
    public Slice<ItemListResponse> getSalesHistory(Long memberId, String status, Pageable pageable) {

        ItemStatus itemStatus = null;

        if (status != null && !status.isBlank()) {
            itemStatus = ItemStatus.valueOf(status.toUpperCase());
        }

        return itemRepository.findAllByMemberIdAndStatus(memberId, itemStatus, pageable)
                .map(item -> ItemListResponse.fromEntity(item, null));
    }

    /**
     * 나의 구매 내역 조회
     */
    public Slice<ItemListResponse> getPurchaseHistory(Long memberId, Pageable pageable) {
        return tradeRepository.findAllByBuyerIdOrderByCreatedAtDesc(memberId, pageable)
                .map(trade -> ItemListResponse.fromEntity(trade.getItem(), trade.getId()));
    }

    /**
     * 나눔 받기 (선착순)
     * 1. 중복 신청 체크
     * 2. 재고 감소 (Atomic Update)
     * 3. 채팅방 자동 생성
     */
    @Transactional
    public Long requestSharing(Long itemId, Long buyerId) {

        Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 1. 아이템 검증
        // ㄴ 비관적 락을 걸고 아이템 조회 (이 시점에 다른 스레드는 대기 상태가 됨)
        //Item item = itemRepository.findById(itemId)
        //        .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));

        // 나눔 상품이 맞는지 확인
        if (item.getItemType() != ItemType.SHARING) {
            throw new BusinessException(ItemErrorCode.INVALID_ITEM_TYPE);
        }

        // 본인이 올린 나눔에 신청하는 것 방지
        if (item.getMember().getId().equals(buyerId)) {
            throw new BusinessException(ItemErrorCode.CANNOT_BUY_MY_ITEM);
        }

        // 2. 중복 신청 방지 (이미 채팅방이 있는지 확인)
        if (chatRoomRepository.findByItemAndBuyer(item, buyer).isPresent()) {
            throw new BusinessException(ItemErrorCode.ALREADY_REQUESTED);
        }

        // 3. 선착순 재고 감소 (핵심 로직)
        // update 쿼리가 1을 반환하면 성공, 0이면 재고가 없다는 뜻
        int updatedCount = itemRepository.decreaseStockAtomic(itemId);

        if (updatedCount == 0) {
            throw new BusinessException(ItemErrorCode.OUT_OF_STOCK); // "선착순 마감되었습니다."
        }

        // 재고 변경 시 상세 캐시 삭제
        redisTemplate.delete("item:details:" + itemId);

        // 4. 성공 시 채팅방 자동 생성
        // ChatService의 createChatRoom 메서드 활용 (없다면 아래 참고하여 추가 필요)
        Long chatRoomId = chatService.createOrGetChatRoom(itemId, buyerId);

        return chatRoomId;
    }

    // --- Helper Methods ---
    private Slice<ItemListResponse> fetchItemListFromDb(ItemSearchCondition condition, Pageable pageable) {
        return itemRepository.searchItems(condition, pageable)
                .map(item -> ItemListResponse.fromEntity(item, null));
    }

    private boolean isMainPageRequest(ItemSearchCondition condition, Pageable pageable) {
        if (condition == null) return pageable.getPageNumber() == 0;
        return condition.getTitle() == null &&
                condition.getMinPrice() == null &&
                condition.getMaxPrice() == null &&
                pageable.getPageNumber() == 0;
    }

    // Type Safety Warning 억제를 위한 헬퍼 메서드
    @SuppressWarnings("unchecked")
    private List<ItemListResponse> getCachedItemList(String key) {
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data instanceof List) {
                return (List<ItemListResponse>) data;
            }
        } catch (Exception e) {
            log.error("Redis Get Failed: {}", e.getMessage());
        }
        return null;
    }

}