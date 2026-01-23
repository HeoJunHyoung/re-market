package com.example.secondhandmarket;

import com.example.secondhandmarket.domain.item.entity.enumerate.Category;
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
@Commit // 테스트가 끝나도 롤백되지 않고 DB에 반영됨
public class BulkInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();
    private final TitleGenerator titleGenerator = new TitleGenerator();

    @Test
    @DisplayName("다양한 카테고리와 상품명을 가진 Mock 데이터 100만개 생성")
    void bulkInsertItems() {
        // 1. 판매자(Member) 생성
        long sellerId = createMockMember();

        // 2. 상품(Item) 데이터 100만개 생성
        int totalCount = 1_000_000;
        int batchSize = 5_000; // 배치 사이즈

        String sql = "INSERT INTO items (seller_id, title, content, price, trade_place, status, category, view_count, chat_count, favorite_count, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?)";

        // 카테고리 전체 목록 가져오기
        Category[] categories = Category.values();

        for (int i = 0; i < totalCount; i += batchSize) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                // 랜덤 카테고리 선택
                Category category = categories[random.nextInt(categories.length)];
                int currentIdx = i + j + 1;

                // 제목과 내용 생성
                String title = titleGenerator.generateTitle(category, currentIdx);
                String content = titleGenerator.generateContent(category, title);

                // 가격은 카테고리별로 다른 범위로 설정
                int price = generatePriceByCategory(category);

                // 거래 장소도 랜덤하게
                String tradePlace = generateTradePlace();

                batchArgs.add(new Object[]{
                        sellerId,
                        title,
                        content,
                        price,
                        tradePlace,
                        "ON_SALE",
                        category.name(),
                        Timestamp.valueOf(LocalDateTime.now().minusMinutes(random.nextInt(100000))),
                        Timestamp.valueOf(LocalDateTime.now())
                });
            }

            // Batch Update 실행
            jdbcTemplate.batchUpdate(sql, batchArgs);
            if ((i + batchSize) % 100_000 == 0) {
                System.out.println("Inserted items: " + (i + batchSize));
            }
        }

        // 3. 상품 이미지(ItemImage) 생성
        System.out.println("Inserting item images...");
        String imageSql = """
            INSERT INTO item_images (item_id, image_url, is_representative, sort_order)
            SELECT item_id, 'mock.png', true, 0
            FROM items
            WHERE seller_id = ?
        """;
        jdbcTemplate.update(imageSql, sellerId);

        System.out.println("Mock Data Generation Completed!");
    }

    private long createMockMember() {
        String username = "tester_1m";
        List<Long> ids = jdbcTemplate.queryForList("SELECT member_id FROM members WHERE username = ?", Long.class, username);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        String sql = "INSERT INTO members (username, password, nickname, phone_number, safety_score, role, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, 300, 'USER', NOW(), NOW())";

        jdbcTemplate.update(sql,
                username,
                "$2a$10$MockPasswordHashValue...",
                "대량테스터",
                "010-0000-0000"
        );

        return jdbcTemplate.queryForObject("SELECT last_insert_id()", Long.class);
    }

    private int generatePriceByCategory(Category category) {
        switch (category) {
            case DIGITAL_DEVICE:
                return (random.nextInt(50) + 10) * 10000; // 10만~60만원
            case WOMEN_CLOTHING:
            case MEN_FASHION_ACCESSORIES:
                return (random.nextInt(30) + 5) * 1000; // 5천~35천원
            case HOME_APPLIANCE:
                return (random.nextInt(100) + 20) * 10000; // 20만~120만원
            case BOOK:
                return (random.nextInt(20) + 2) * 1000; // 2천~22천원
            case TICKET_VOUCHER:
                return (random.nextInt(5) + 1) * 10000; // 1만~6만원
            default:
                return (random.nextInt(50) + 5) * 1000; // 5천~55천원
        }
    }

    private String generateTradePlace() {
        String[] districts = {
                "서울 강남구 역삼동", "서울 서초구 서초동", "서울 송파구 잠실동",
                "서울 마포구 합정동", "서울 영등포구 여의도동", "서울 종로구 종로",
                "부산 해운대구 우동", "인천 연수구 송도동", "대구 중구 동인동",
                "광주 서구 화정동", "대전 서구 둔산동", "울산 남구 삼산동"
        };
        return districts[random.nextInt(districts.length)];
    }
}

