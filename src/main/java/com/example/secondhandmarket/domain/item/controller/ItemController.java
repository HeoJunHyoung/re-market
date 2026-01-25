package com.example.secondhandmarket.domain.item.controller;

import com.example.secondhandmarket.domain.chat.dto.response.ChatRoomResponse;
import com.example.secondhandmarket.domain.chat.service.ChatService;
import com.example.secondhandmarket.domain.item.dto.request.ItemCreateRequest;
import com.example.secondhandmarket.domain.item.dto.request.ItemSearchCondition;
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
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final ChatService chatService;

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
    public ResponseEntity<Map<String, Long>> updateItemStatus(@AuthenticationPrincipal AuthMember authMember,
                                                 @PathVariable Long itemId, @RequestBody ItemStatusUpdateRequest request) {
        Long tradeId = itemService.updateItemStatus(authMember.getMemberId(), itemId, request);

        // tradeId가 있으면 JSON으로 반환 { "tradeId": 1 }
        if (tradeId != null) {
            return ResponseEntity.ok(Map.of("tradeId", tradeId));
        } else {
            return ResponseEntity.ok(Map.of());
        }

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
    public ResponseEntity<Slice<ItemListResponse>> getItemList(@ModelAttribute ItemSearchCondition condition, @PageableDefault(size = 20) Pageable pageable) {
        Slice<ItemListResponse> itemListResponses = itemService.getItemList(condition, pageable);
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

    /**
     * 나의 판매 내역 조회 API
     */
    @GetMapping("/my-sales")
    public ResponseEntity<Slice<ItemListResponse>> getMySalesItems(@AuthenticationPrincipal AuthMember authMember,
                                                                   @RequestParam(required = false) String status,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        Slice<ItemListResponse> responses = itemService.getSalesHistory(authMember.getMemberId(), status, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<Slice<ItemListResponse>> getMyPurchaseItems(@AuthenticationPrincipal AuthMember authMember,
                                                                      @PageableDefault(size = 20) Pageable pageable) {
        Slice<ItemListResponse> responses = itemService.getPurchaseHistory(authMember.getMemberId(), pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{itemId}/share")
    public ResponseEntity<Map<String, Long>> requestSharing(@AuthenticationPrincipal AuthMember authMember,
                                                            @PathVariable Long itemId) {
        // 서비스 호출 (재고 감소 -> 채팅방 생성)
        Long chatRoomId = itemService.requestSharing(itemId, authMember.getMemberId());

        // 생성된 채팅방 ID 반환 (프론트에서 바로 채팅방으로 이동하기 위함)
        return ResponseEntity.ok(Map.of("chatRoomId", chatRoomId));
    }

    @GetMapping("/{itemId}/chats")
    public ResponseEntity<List<ChatRoomResponse>> getItemChatRooms(@AuthenticationPrincipal AuthMember authMember,
                                                                   @PathVariable Long itemId) {
        List<ChatRoomResponse> chatRooms = chatService.getChatRoomsByItem(itemId, authMember.getMemberId());
        return ResponseEntity.ok(chatRooms);
    }

}
