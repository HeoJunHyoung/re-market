package com.example.remarket.domain.item.service;

import com.example.remarket.domain.item.dto.request.ItemUpdateRequest;
import com.example.remarket.domain.item.entity.ItemImage;
import com.example.remarket.domain.item.exception.ItemErrorCode;
import com.example.remarket.global.error.BusinessException;
import com.example.remarket.global.util.FileUtil;
import com.example.remarket.domain.chat.repository.jpa.ChatRoomRepository;
import com.example.remarket.domain.chat.service.ChatService;
import com.example.remarket.domain.item.dto.request.ItemCreateRequest;
import com.example.remarket.domain.item.dto.request.ItemSearchCondition;
import com.example.remarket.domain.item.dto.request.ItemStatusUpdateRequest;
import com.example.remarket.domain.item.dto.response.ItemDetailsResponse;
import com.example.remarket.domain.item.dto.response.ItemListResponse;
import com.example.remarket.domain.item.entity.Favorite;
import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.item.entity.enumerate.ItemStatus;
import com.example.remarket.domain.item.entity.enumerate.ItemType;
import com.example.remarket.domain.item.repository.FavoriteRepository;
import com.example.remarket.domain.item.repository.ItemRepository;
import com.example.remarket.domain.member.entity.Member;
import com.example.remarket.domain.member.exception.MemberErrorCode;
import com.example.remarket.domain.member.repository.MemberRepository;
import com.example.remarket.domain.trade.entity.Trade;
import com.example.remarket.domain.trade.repository.TradeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MeterRegistry meterRegistry;

    // 캐시 설정 상수
    private static final String MAIN_PAGE_CACHE_KEY = "main:items:page:0";
    private static final long MAIN_PAGE_TTL = 5L; // 메인 페이지 캐시 5초
    // 락 대기 시간을 10초로 설정하여, 먼저 진입한 스레드가 끝날 때까지 충분히 기다리게 함

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

        // [모니터링]
        Counter.builder("business.item.registered")    // 1. 측정기 이름 정하기
                .tag("category", request.getCategory().name()) // 2. 카테고리 태그 붙이기 (전자제품, 의류...)
                .tag("type", request.getItemType().name())     // 3. 타입 태그 붙이기 (판매, 나눔)
                .description("신규 상품 등록 수")
                .register(meterRegistry)               // 4. 장부에 등록하기
                .increment();                          // 5. 숫자 1 올리기

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

            // [모니터링] 거래 완료 횟수 및 금액 합계
            Counter.builder("business.trade.completed")
                    .description("거래 완료(판매 성사) 횟수")
                    .register(meterRegistry)
                    .increment();

            // [모니터링] 판매 금액 누적 (매출액 파악용)
            Counter.builder("business.trade.amount")
                    .baseUnit("KRW")
                    .description("총 거래 완료 금액")
                    .register(meterRegistry)
                    .increment(item.getPrice()); // 1 대신 판매 가격만큼 올림

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
     * 상품 수정
     */
    @Transactional
    public void updateItem(Long memberId, Long itemId, ItemUpdateRequest request, List<MultipartFile> newImages) throws IOException {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));

        // 1. 작성자 권한 검증
        if (!item.getMember().getId().equals(memberId)) {
            throw new BusinessException(ItemErrorCode.NOT_ITEM_OWNER);
        }

        // 2. 기본 정보 수정 (제목, 내용, 가격 등)
        item.updateItem(
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getTradePlace(),
                request.getCategory()
        );

        // 3. 이미지 삭제 처리
        List<Long> deletedImageIds = request.getDeletedImageIds();
        if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
            // 리스트를 순회하며 삭제 대상 제거 (removeIf 사용)
            item.getItemImages().removeIf(img -> {
                if (deletedImageIds.contains(img.getId())) {
                    return true; // 리스트에서 제거
                }
                return false;
            });
        }

        // 4. 새 이미지 추가 처리
        if (newImages != null && !newImages.isEmpty()) {
            List<String> storedFileNames = fileUtil.storeFiles(newImages);
            for (String storedName : storedFileNames) {
                // 임시 순서로 추가 (나중에 재정렬)
                ItemImage newImage = ItemImage.createItemImage(storedName, false, 999);
                item.addItemImage(newImage);
            }
        }

        // 5. 대표 이미지 재설정 및 순서 정렬 (0번 인덱스가 대표 이미지)
        List<ItemImage> currentImages = item.getItemImages();
        if (!currentImages.isEmpty()) {
            for (int i = 0; i < currentImages.size(); i++) {
                ItemImage img = currentImages.get(i);
            }
        }

        // 6. 캐시 삭제
        redisTemplate.delete("item:details:" + itemId);
        redisTemplate.delete(MAIN_PAGE_CACHE_KEY);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteItem(Long memberId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));

        if (!item.getMember().getId().equals(memberId)) {
            throw new BusinessException(ItemErrorCode.NOT_ITEM_OWNER);
        }

        // DB 삭제
        itemRepository.delete(item);

        // 캐시 삭제
        redisTemplate.delete("item:details:" + itemId);
        redisTemplate.delete(MAIN_PAGE_CACHE_KEY);
    }

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

        // [모니터링] 1. 스톱워치 시작
        Timer.Sample sample = Timer.start(meterRegistry);
        String resultTag = "FAIL"; // 기본값은 '실패'로 설정

        try {
            // ----------------- 기존 비즈니스 로직 시작 -----------------
            Member buyer = memberRepository.findById(buyerId)
                    .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

            // 1. 아이템 검증 (비관적 락)
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

            // 2. 중복 신청 방지
            if (chatRoomRepository.findByItemAndBuyer(item, buyer).isPresent()) {
                throw new BusinessException(ItemErrorCode.ALREADY_REQUESTED);
            }

            // 3. 선착순 재고 감소 (핵심 로직)
            int updatedCount = itemRepository.decreaseStockAtomic(itemId);

            if (updatedCount == 0) {
                throw new BusinessException(ItemErrorCode.OUT_OF_STOCK);
            }

            // 재고 변경 시 상세 캐시 삭제
            redisTemplate.delete("item:details:" + itemId);

            // 4. 성공 시 채팅방 자동 생성
            Long chatRoomId = chatService.createOrGetChatRoom(itemId, buyerId);

            // 여기까지 오면 성공! 태그를 변경
            resultTag = "SUCCESS";

            return chatRoomId;
            // ----------------- 기존 비즈니스 로직 끝 -----------------

        } finally {
            // [모니터링] 2. 로직이 끝나면(성공이든 에러든) 무조건 기록
            sample.stop(Timer.builder("business.sharing.request")
                    .tag("result", resultTag) // 성공/실패 여부 기록
                    .description("나눔 선착순 신청 처리 시간")
                    .register(meterRegistry));
        }
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