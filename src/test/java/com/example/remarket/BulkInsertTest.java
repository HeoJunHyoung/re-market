package com.example.remarket;

import com.example.remarket.domain.item.entity.enumerate.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
@Transactional
@Commit
public class BulkInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();
    private final TitleGenerator titleGenerator = new TitleGenerator();

    // 평택시 중앙동 기준 거리별 동네 목록 (경기도 좌표 데이터 기반)
    private final String[] LEVEL_1_NEIGHBORHOODS = { // 1.5km 이내
            "경기도 평택시 중앙동", "경기도 평택시 이충동", "경기도 평택시 장당동", "경기도 평택시 서정동"
    };

    private final String[] LEVEL_2_NEIGHBORHOODS = { // 1.5 ~ 3.0km
            "경기도 평택시 장안동", "경기도 평택시 가재동", "경기도 평택시 신장2동",
            "경기도 평택시 송북동", "경기도 평택시 지산동"
    };

    private final String[] LEVEL_3_NEIGHBORHOODS = { // 3.0 ~ 5.0km
            "경기도 평택시 지제동", "경기도 평택시 신장동", "경기도 평택시 독곡동",
            "경기도 평택시 도일동", "경기도 평택시 칠괴동", "경기도 평택시 모곡동",
            "경기도 평택시 신장1동", "경기도 평택시 송탄동", "경기도 평택시 고덕면"
    };

    @Test
    @DisplayName("평택시 중앙동 기준으로 거리별 데이터 분포를 맞춘 Mock 데이터 100만개 생성")
    void bulkInsertItems() {
        // 1. 판매자(Member) 생성 (위치를 평택시 중앙동으로 설정)
        long sellerId = createMockMember();

        // 2. 상품(Item) 데이터 100만개 생성
        int totalCount = 1_000_000; // 100만 개
        int batchSize = 5_000;      // 배치 사이즈 5000으로 증가

        String sql = "INSERT INTO items (seller_id, title, content, price, trade_place, status, category, item_type, stock_quantity, view_count, chat_count, favorite_count, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?)";

        Category[] categories = Category.values();

        // 데이터 분포 설정 (총 100만)
        // Level 1: 0 ~ 50,000 (5만 개)
        // Level 2: 50,000 ~ 950,000 (90만 개) -> 누적 95만
        // Level 3: 950,000 ~ 1,000,000 (5만 개) -> 누적 100만
        int level1Limit = 50_000;
        int level2Limit = 950_000;

        for (int i = 0; i < totalCount; i += batchSize) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                int currentIdx = i + j; // 0부터 시작

                // 랜덤 카테고리
                Category category = categories[random.nextInt(categories.length)];

                // 제목과 내용
                String title = titleGenerator.generateTitle(category, currentIdx + 1);
                String content = titleGenerator.generateContent(category, title);

                // 가격
                int price = generatePriceByCategory(category);

                // 인덱스 구간에 따라 거래 장소(동네) 배정
                String tradePlace;
                if (currentIdx < level1Limit) {
                    // 0 ~ 49,999 (5만개)
                    tradePlace = LEVEL_1_NEIGHBORHOODS[random.nextInt(LEVEL_1_NEIGHBORHOODS.length)];
                } else if (currentIdx < level2Limit) {
                    // 50,000 ~ 949,999 (90만개)
                    tradePlace = LEVEL_2_NEIGHBORHOODS[random.nextInt(LEVEL_2_NEIGHBORHOODS.length)];
                } else {
                    // 950,000 ~ 999,999 (5만개)
                    tradePlace = LEVEL_3_NEIGHBORHOODS[random.nextInt(LEVEL_3_NEIGHBORHOODS.length)];
                }

                batchArgs.add(new Object[]{
                        sellerId,
                        title,
                        content,
                        price,
                        tradePlace,
                        "ON_SALE",
                        category.name(),
                        "SALE",
                        1,
                        Timestamp.valueOf(LocalDateTime.now().minusMinutes(random.nextInt(500000))), // 시간 분산 확대
                        Timestamp.valueOf(LocalDateTime.now())
                });
            }

            jdbcTemplate.batchUpdate(sql, batchArgs);
            if ((i + batchSize) % 100_000 == 0) {
                System.out.println("Inserted items: " + (i + batchSize));
            }
        }

        // 3. 상품 이미지 생성 (대표 이미지 1개씩)
        System.out.println("Inserting item images...");
        // 100만개 데이터에 대해 이미지 삽입은 시간이 걸릴 수 있으므로 쿼리 최적화 유지
        String imageSql = """
            INSERT INTO item_images (item_id, image_url, is_representative, sort_order)
            SELECT item_id, 'mock.png', true, 0
            FROM items
            WHERE seller_id = ?
        """;
        jdbcTemplate.update(imageSql, sellerId);

        System.out.println("Mock Data Generation Completed! (Total: " + totalCount + ")");
    }

    private long createMockMember() {
        String username = "mock_seller";
        List<Long> ids = jdbcTemplate.queryForList("SELECT member_id FROM members WHERE username = ?", Long.class, username);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        // 판매자 위치: 경기도 평택시 중앙동
        String sql = "INSERT INTO members (username, password, nickname, phone_number, safety_score, role, is_location_verified, city, district, neighborhood, version, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, 300, 'USER', 1, ?, ?, ?, 0, NOW(), NOW())";

        jdbcTemplate.update(sql,
                username,
                "$2a$10$gPrBypZokPwFnSKPptfhLeW0E45bhXAMmFztG74pgHUForE8A2dc.", // password
                "평택판매자",
                "010-0000-0000",
                "경기도",
                "평택시",
                "중앙동"
        );

        return jdbcTemplate.queryForObject("SELECT last_insert_id()", Long.class);
    }

    private int generatePriceByCategory(Category category) {
        switch (category) {
            case DIGITAL_DEVICE:
                return (random.nextInt(50) + 10) * 10000;
            case WOMEN_CLOTHING:
            case MEN_FASHION_ACCESSORIES:
                return (random.nextInt(30) + 5) * 1000;
            case HOME_APPLIANCE:
                return (random.nextInt(100) + 20) * 10000;
            case BOOK:
                return (random.nextInt(20) + 2) * 1000;
            case TICKET_VOUCHER:
                return (random.nextInt(5) + 1) * 10000;
            default:
                return (random.nextInt(50) + 5) * 1000;
        }
    }
}