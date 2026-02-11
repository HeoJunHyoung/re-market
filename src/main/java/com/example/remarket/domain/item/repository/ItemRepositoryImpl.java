package com.example.remarket.domain.item.repository;

import com.example.remarket.domain.item.entity.Item;
import com.example.remarket.domain.item.dto.request.ItemSearchCondition;
import com.example.remarket.domain.item.entity.enumerate.ItemStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.remarket.domain.item.entity.QItem.item;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Item> searchItems(ItemSearchCondition condition, List<String> targetRegions, Pageable pageable) {

        // 검색어가 있을 경우 정확도(Score) 템플릿 생성
        NumberTemplate<Double> matchScore = null;
        if (StringUtils.hasText(condition.getTitle())) {
            // "어센틱 255" -> "+어센틱 +255" 로 변환 (Boolean Mode 형식)
            String formattedTitle = formatToBooleanMode(condition.getTitle());
            matchScore = Expressions.numberTemplate(Double.class,
                    "function('match_boolean', {0}, {1})", item.title, formattedTitle);
        }

        List<Item> content = queryFactory
                .selectFrom(item)
                .where(
                        titleMatches(matchScore), // 수정된 메서드 사용
                        priceGreaterThanOrEqual(condition.getMinPrice()),
                        priceLowerThanOrEqual(condition.getMaxPrice()),
                        statusEq(condition.getStatus()),
                        regionIn(targetRegions)
                )
                .orderBy(
                        // 검색어가 있으면 정확도순 정렬 우선, 그 다음 기존 정렬 조건
                        getOrderSpecifier(condition.getSort(), matchScore)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return checkLastPage(pageable, content);
    }

    // 검색어 포맷팅: 공백을 기준으로 쪼개서 앞에 '+'를 붙임 (AND 조건)
    private String formatToBooleanMode(String title) {
        if (!StringUtils.hasText(title)) return "";
        String[] words = title.trim().split("\\s+");
        // "word1 word2" -> "+word1 +word2"
        return "+" + String.join(" +", words);
    }

    // BooleanExpression 생성 (점수가 0보다 큰 것만)
    private BooleanExpression titleMatches(NumberTemplate<Double> matchScore) {
        return matchScore != null ? matchScore.gt(0) : null;
    }

    private BooleanExpression priceGreaterThanOrEqual(Integer price) {
        return price != null ? item.price.goe(price) : null;
    }

    private BooleanExpression priceLowerThanOrEqual(Integer price) {
        return price != null ? item.price.loe(price) : null;
    }

    private BooleanExpression statusEq(ItemStatus status) {
        return status != null ? item.status.eq(status) : null;
    }

    private BooleanExpression regionIn(List<String> regions) {
        return (regions != null && !regions.isEmpty()) ? item.member.address.neighborhood.in(regions) : null;
    }

    // 정렬 로직 수정
    private OrderSpecifier<?>[] getOrderSpecifier(String sort, NumberTemplate<Double> matchScore) {
        OrderSpecifier<?> defaultSort = item.createdAt.desc();

        // 정렬 조건이 명시된 경우 (가격순 등) -> 해당 조건 우선
        if (StringUtils.hasText(sort)) {
            OrderSpecifier<?> specificSort = switch (sort) {
                case "lowPrice"  -> item.price.asc();
                case "highPrice" -> item.price.desc();
                default          -> item.createdAt.desc();
            };
            return new OrderSpecifier<?>[]{ specificSort, defaultSort };
        }

        // 정렬 조건이 없고 검색어가 있는 경우 -> 정확도순(DESC) 우선, 그 다음 최신순
        if (matchScore != null) {
            return new OrderSpecifier<?>[]{ matchScore.desc(), defaultSort };
        }

        // 그 외 -> 최신순
        return new OrderSpecifier<?>[]{ defaultSort };
    }

    private Slice<Item> checkLastPage(Pageable pageable, List<Item> content) {
        boolean hasNext = false;

        if (content.size() > pageable.getPageSize()) {
            hasNext = true;
            content.remove(pageable.getPageSize()); // 확인용으로 가져온 마지막 데이터 제거
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

}
