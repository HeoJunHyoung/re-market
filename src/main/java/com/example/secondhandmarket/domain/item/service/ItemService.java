package com.example.secondhandmarket.domain.item.service;

import com.example.secondhandmarket.domain.item.dto.request.ItemCreateRequest;
import com.example.secondhandmarket.domain.item.dto.response.ItemDetailsResponse;
import com.example.secondhandmarket.domain.item.dto.response.ItemListResponse;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.ItemImage;
import com.example.secondhandmarket.domain.item.exception.ItemErrorCode;
import com.example.secondhandmarket.domain.item.repository.ItemRepository;
import com.example.secondhandmarket.domain.member.entity.Member;
import com.example.secondhandmarket.domain.member.exception.MemberErrorCode;
import com.example.secondhandmarket.domain.member.repository.MemberRepository;
import com.example.secondhandmarket.global.error.BusinessException;
import com.example.secondhandmarket.global.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final FileUtil fileUtil;

    /**
     * 상품 등록
     */
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
                item.addImage(itemImage);
            }
        }

        // 4. 저장 (Cascade로 인해 ItemImage도 함께 저장됨)
        itemRepository.save(item);
    }

//    public Slice<ItemListResponse> getItemList() {
//
//    }

//    public ItemDetailsResponse getItemDetails(Long itemId) {
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));
//
//    }

}
