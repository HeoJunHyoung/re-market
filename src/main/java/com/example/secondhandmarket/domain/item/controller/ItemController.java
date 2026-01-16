package com.example.secondhandmarket.domain.item.controller;

import com.example.secondhandmarket.domain.item.dto.request.ItemCreateRequest;
import com.example.secondhandmarket.domain.item.dto.request.ItemStatusUpdateRequest;
import com.example.secondhandmarket.domain.item.dto.response.ItemDetailsResponse;
import com.example.secondhandmarket.domain.item.dto.response.ItemListResponse;
import com.example.secondhandmarket.domain.item.service.ItemService;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    /**
     * 상품 등록
     */
    @PostMapping
    public ResponseEntity<Void> registerItem(@AuthenticationPrincipal AuthMember authMember,
                                             @RequestPart(value = "data") ItemCreateRequest request,
                                             @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) throws IOException {
        itemService.registerItem(authMember.getMemberId(), request, imageFiles);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 상품 상태 변경 (판매중 / 예약중 / 거래완료)
     */
    @PatchMapping("/{itemId}/status")
    public ResponseEntity<Void> updateItemStatus(@AuthenticationPrincipal AuthMember authMember,
                                                 @PathVariable Long itemId, @RequestBody ItemStatusUpdateRequest request) {
        itemService.updateItemStatus(authMember.getMemberId(), itemId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 상품 수정
     */


    /**
     * 상품 삭제
     */


    /**
     * 상품 전체 조회
     */
    @GetMapping
    public ResponseEntity<Slice<ItemListResponse>> getItemList(@PageableDefault(size = 20) Pageable pageable) {
        Slice<ItemListResponse> itemListResponses = itemService.getItemList(pageable);
        return ResponseEntity.ok(itemListResponses);
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailsResponse> getItemDetails(@AuthenticationPrincipal AuthMember authMember,
                                                              @PathVariable("itemId") Long itemId) {
        Long memberId = (authMember != null) ? authMember.getMemberId() : null;
        ItemDetailsResponse itemDetailsResponse = itemService.getItemDetails(itemId, memberId);

        return ResponseEntity.ok(itemDetailsResponse);
    }

    /**
     * 관심 상품 등록/해제 토글
     */
    @PostMapping("{itemId}/favorites")
    public ResponseEntity<Void> toggleFavorite(@AuthenticationPrincipal AuthMember authMember,
                                               @PathVariable("itemId") Long itemId) {
        itemService.toggleFavorite(authMember.getMemberId(), itemId);
        return ResponseEntity.ok().build();
    }

    /**
     * 나의 관심 상품 목록 조회
     */
    @GetMapping("/favorites")
    public ResponseEntity<Slice<ItemListResponse>> getMyFavoriteItems(@AuthenticationPrincipal AuthMember authMember,
                                                                      @PageableDefault(size = 20) Pageable pageable) {
        Slice<ItemListResponse> myFavoriteItems = itemService.getMyFavoriteItems(authMember.getMemberId(), pageable);
        return ResponseEntity.ok(myFavoriteItems);
    }
}
