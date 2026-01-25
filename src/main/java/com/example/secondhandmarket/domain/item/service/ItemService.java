package com.example.secondhandmarket.domain.item.service;

import com.example.secondhandmarket.domain.item.dto.request.ItemCreateRequest;
import com.example.secondhandmarket.domain.item.dto.request.ItemSearchCondition;
import com.example.secondhandmarket.domain.item.dto.request.ItemStatusUpdateRequest;
import com.example.secondhandmarket.domain.item.dto.response.ItemDetailsImageResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final TradeRepository tradeRepository;
    private final FileUtil fileUtil;

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

            return trade.getId();

        } else {
            // 판매중 / 예약중: 상태만 변경 (구매자 정보 초기화)
            item.changeStatus(newStatus);
            return null;
        }
    }

    /**
     * 상품 수정
     */

    /**
     * 상품 삭제
     */

    /**
     * 상품 목록 조회
     */
    public Slice<ItemListResponse> getItemList(ItemSearchCondition condition, Pageable pageable) {
        return itemRepository.searchItems(condition, pageable)
                .map(item -> ItemListResponse.fromEntity(item, null));
    }

    /**
     * 상품 상세 조회
     */
    @Transactional
    public ItemDetailsResponse getItemDetails(Long itemId, Long memberId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));

        item.increaseViewCount();

        boolean isFavorite = false;
        if (memberId != null) {
            // 로그인한 경우 찜 여부 확인
            Member member = memberRepository.getReferenceById(memberId); // 프록시 조회로 성능 최적화
            isFavorite = favoriteRepository.findByMemberAndItem(member, item).isPresent();
        }

        return ItemDetailsResponse.fromEntity(item, isFavorite);
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
                            item.decreaseFavoriteCount();
                        },
                        () -> {
                            // 없으면 관심 등록
                            Favorite favorite = Favorite.createFavorite(member, item);
                            favoriteRepository.save(favorite);
                            item.increaseFavoriteCount();
                        }
                );
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

}
