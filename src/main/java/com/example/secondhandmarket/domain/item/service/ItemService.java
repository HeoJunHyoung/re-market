package com.example.secondhandmarket.domain.item.service;

import com.example.secondhandmarket.domain.item.dto.request.ItemCreateRequest;
import com.example.secondhandmarket.domain.item.dto.response.ItemDetailsImageResponse;
import com.example.secondhandmarket.domain.item.dto.response.ItemDetailsResponse;
import com.example.secondhandmarket.domain.item.dto.response.ItemListResponse;
import com.example.secondhandmarket.domain.item.entity.Favorite;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.ItemImage;
import com.example.secondhandmarket.domain.item.exception.ItemErrorCode;
import com.example.secondhandmarket.domain.item.repository.FavoriteRepository;
import com.example.secondhandmarket.domain.item.repository.ItemRepository;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.exception.MemberErrorCode;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
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
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final FileUtil fileUtil;

    /**
     * 상품 등록
     */
    @Transactional
    public void registerItem(Long memberId, ItemCreateRequest request, List<MultipartFile> imageFiles) throws IOException {

        // 1. 판매자 조회
        Member seller = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. 상품 생성
        Item item = Item.createItem(
                seller,
                request.getTitle(),
                request.getContent(),
                request.getPrice(),
                request.getTradePlace(),
                request.getCategory()
        );

        // 3. 이미지 저장 및 연관관계 설정
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> storedFileNames = fileUtil.storeFiles(imageFiles);

            for (int i = 0; i < storedFileNames.size(); i++) {
                String storedName = storedFileNames.get(i);
                boolean isRepresentative = (i == 0); // 첫 번째 사진을 대표 이미지로

                // ItemImage 생성
                ItemImage itemImage = ItemImage.createItemImage(storedName, isRepresentative, i);

                // 연관관계 편의 메서드 호출
                item.addItemImage(itemImage);
            }
        }

        // 4. 저장 (Cascade로 인해 ItemImage도 함께 저장됨)
        itemRepository.save(item);
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
    public Slice<ItemListResponse> getItemList(Pageable pageable) {

        // 1. 최신순 상품들 조회
        Slice<Item> latestItems = itemRepository.findLatestItems(pageable);

        // 2. 목록 응답 DTO로 변환
        return latestItems.map(
                item -> {
                    String thumbnailImageUrl = item.getItemImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsRepresentative()))
                            .findFirst()
                            .map(i -> i.getImageUrl())
                            .orElse(null);

                    return ItemListResponse.builder()
                            .itemId(item.getId())
                            .title(item.getTitle())
                            .tradePlace(item.getTradePlace())
                            .price(item.getPrice())
                            .thumbnailUrl(thumbnailImageUrl)
                            .status(item.getStatus())
                            .chatCount(item.getChatCount())
                            .favoriteCount(item.getFavoriteCount())
                            .createdAt(item.getCreatedAt())
                            .build();
                }
        );
    }

    /**
     * 상품 상세 조회
     */
    @Transactional
    public ItemDetailsResponse getItemDetails(Long itemId) {

        // 1. 상품 ID 기반 상세 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));

        // 2. 조회수 증가
        item.increaseViewCount();

        // 3. 이미지 DTO 변환
        List<ItemDetailsImageResponse> imageResponses = item.getItemImages().stream()
                .map(img -> ItemDetailsImageResponse.builder()
                        .itemImageId(img.getId())
                        .itemId(item.getId())
                        .imageUrl(img.getImageUrl())
                        .isRepresentative(img.getIsRepresentative())
                        .sortOrder(img.getSortOrder())
                        .build())
                .toList();

        return ItemDetailsResponse.builder()
                .itemId(item.getId())
                .sellerId(item.getMember().getId())
                .sellerNickname(item.getMember().getNickname())
                .sellerAddress(item.getMember().getAddress())
                .sellerSafetyScore(item.getMember().getSafetyScore())
                .title(item.getTitle())
                .content(item.getContent())
                .price(item.getPrice())
                .favoriteCount(item.getFavoriteCount())
                .chatCount(item.getChatCount())
                .viewCount(item.getViewCount())
                .tradePlace(item.getTradePlace())
                .status(item.getStatus())
                .category(item.getCategory())
                .itemImages(imageResponses)
                .build();
    }

    /**
     * 관심 상품 등록/해제 토글
     */
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

        Slice<Item> myFavoriteItems = favoriteRepository.findMyFavoriteItems(memberId, pageable);

        return myFavoriteItems.map(item -> {
            String thumbnailImageUrl = item.getItemImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsRepresentative()))
                    .findFirst()
                    .map(ItemImage::getImageUrl)
                    .orElse(null);

            return ItemListResponse.builder()
                    .itemId(item.getId())
                    .title(item.getTitle())
                    .tradePlace(item.getTradePlace())
                    .price(item.getPrice())
                    .thumbnailUrl(thumbnailImageUrl)
                    .status(item.getStatus())
                    .chatCount(item.getChatCount())
                    .favoriteCount(item.getFavoriteCount())
                    .createdAt(item.getCreatedAt())
                    .build();
        });
    }
}
