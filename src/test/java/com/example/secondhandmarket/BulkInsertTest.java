package com.example.secondhandmarket;

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

@SpringBootTest
@Transactional
@Commit // 테스트가 끝나도 롤백되지 않고 DB에 반영됨
public class BulkInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("성능 테스트용 Mock 데이터 100만개 생성")
    void bulkInsertItems() {
        // 1. 판매자(Member) 생성 (이미 존재하면 skip 가능)
        long sellerId = createMockMember();

        // 2. 상품(Item) 데이터 100만개 생성
        //    - 배치 사이즈: 5000개씩 끊어서 DB에 전송 (메모리 효율 및 속도 고려)
        int totalCount = 1_000_000;
        int batchSize = 5_000;

        String sql = "INSERT INTO items (seller_id, title, content, price, trade_place, status, category, view_count, chat_count, favorite_count, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?)";

        for (int i = 0; i < totalCount; i += batchSize) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                int currentIdx = i + j + 1;
                batchArgs.add(new Object[]{
                        sellerId,
                        "Mock 상품 제목 " + currentIdx,        // title
                        "성능 최적화 테스트를 위한 Mock 상품 내용입니다. 번호: " + currentIdx, // content
                        (int) (Math.random() * 100000) + 1000, // price (1000 ~ 100000원 랜덤)
                        "서울 강남구 역삼동",                  // trade_place
                        "ON_SALE",                             // status (판매중)
                        "DIGITAL_DEVICE",                      // category (디지털기기)
                        Timestamp.valueOf(LocalDateTime.now()), // created_at
                        Timestamp.valueOf(LocalDateTime.now())  // last_modified_at
                });
            }

            // Batch Update 실행
            jdbcTemplate.batchUpdate(sql, batchArgs);
            System.out.println("Inserted items: " + (i + batchSize));
        }

        // 3. 상품 이미지(ItemImage) 생성
        //    - 100만 건을 하나씩 넣는 것보다, 방금 넣은 Item들을 기반으로 한 번에 복사하는 것이 훨씬 빠릅니다.
        //    - image_url에 'mock.png'를 넣어 C:\images\mock.png를 가리키게 합니다.
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
        // 이미 존재하는지 확인
        List<Long> ids = jdbcTemplate.queryForList("SELECT member_id FROM members WHERE username = ?", Long.class, username);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        String sql = "INSERT INTO members (username, password, nickname, phone_number, safety_score, role, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, 300, 'USER', NOW(), NOW())";

        jdbcTemplate.update(sql,
                username,
                "$2a$10$MockPasswordHashValue...", // BCrypt 암호화된 더미 비번
                "대량테스터",
                "010-0000-0000"
        );

        return jdbcTemplate.queryForObject("SELECT last_insert_id()", Long.class);
    }
}