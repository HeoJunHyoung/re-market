package com.example.secondhandmarket.domain.item.repository;

import com.example.secondhandmarket.domain.item.dto.request.ItemSearchCondition;
import com.example.secondhandmarket.domain.item.entity.Item;
import com.example.secondhandmarket.domain.item.entity.QItem;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.secondhandmarket.domain.item.entity.QItem.item;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Item> searchItems(ItemSearchCondition condition, Pageable pageable) {

        List<Item> content = queryFactory
                .selectFrom(item)
                .where(
                        titleContains(condition.getTitle()),
                        priceGreaterThanOrEqual(condition.getMinPrice()),
                        priceLowerThanOrEqual(condition.getMaxPrice())
                )
                .orderBy(getOrderSpecifier(condition.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return checkLastPage(pageable, content);

    }

    private BooleanExpression titleContains(String title) {
//        return StringUtils.hasText(title) ? item.title.contains(title) : null;
        if (!StringUtils.hasText(title)) {
            return null;
        }
        // MySQL의 MATCH AGAINST 구문을 사용하도록 템플릿 작성
        return Expressions.numberTemplate(Double.class,
                "function('match', {0}, {1})", item.title, title).gt(0);
    }

    private BooleanExpression priceGreaterThanOrEqual(Integer price) {
        return price != null ? item.price.goe(price) : null;
    }

    private BooleanExpression priceLowerThanOrEqual(Integer price) {
        return price != null ? item.price.loe(price) : null;
    }

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (!StringUtils.hasText(sort)) return item.createdAt.desc();

        return switch (sort) {
            case "lowPrice"  -> item.price.asc();
            case "highPrice" -> item.price.desc();
            default          -> item.createdAt.desc();
        };
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
