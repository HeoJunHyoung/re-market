package com.example.secondhandmarket.domain.item.controller;

import com.example.secondhandmarket.domain.item.dto.request.ItemCreateRequest;
import com.example.secondhandmarket.domain.item.dto.response.ItemDetailsResponse;
import com.example.secondhandmarket.domain.item.dto.response.ItemListResponse;
import com.example.secondhandmarket.domain.item.service.ItemService;
import com.example.secondhandmarket.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    public ResponseEntity<ItemDetailsResponse> getItemDetails(@PathVariable("itemId") Long itemId) {
        ItemDetailsResponse itemDetailsResponse = itemService.getItemDetails(itemId);
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
