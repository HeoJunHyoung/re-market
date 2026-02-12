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
import java.util.*;

@SpringBootTest
@Transactional
@Commit
public class BulkInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();
    private final TitleGenerator titleGenerator = new TitleGenerator();

    // 동네 이름 -> 판매자 ID 매핑용 맵
    private final Map<String, Long> neighborhoodSellerMap = new HashMap<>();

    // 평택시 중앙동 기준 거리별 동네 목록
    private final String[] LEVEL_1_NEIGHBORHOODS = {
            "경기도 평택시 중앙동", "경기도 평택시 이충동", "경기도 평택시 장당동", "경기도 평택시 서정동"
    };

    private final String[] LEVEL_2_NEIGHBORHOODS = {
            "경기도 평택시 장안동", "경기도 평택시 가재동", "경기도 평택시 신장2동",
            "경기도 평택시 송북동", "경기도 평택시 지산동"
    };

    private final String[] LEVEL_3_NEIGHBORHOODS = {
            "경기도 평택시 지제동", "경기도 평택시 신장동", "경기도 평택시 독곡동",
            "경기도 평택시 도일동", "경기도 평택시 칠괴동", "경기도 평택시 모곡동",
            "경기도 평택시 신장1동", "경기도 평택시 송탄동", "경기도 평택시 고덕면"
    };

    @Test
    @DisplayName("평택시 중앙동 기준으로 거리별 데이터 분포를 맞춘 Mock 데이터 100만개 생성")
    void bulkInsertItems() {
        // 1. 각 동네별 판매자(Member) 생성 및 ID 매핑
        // 중앙동, 이충동 등 각 동네마다 1명의 대표 판매자 생성
        setupSellers(LEVEL_1_NEIGHBORHOODS);
        setupSellers(LEVEL_2_NEIGHBORHOODS);
        setupSellers(LEVEL_3_NEIGHBORHOODS);

        // 2. 상품(Item) 데이터 100만개 생성
        int totalCount = 1_000_000;
        int batchSize = 5_000;

        String sql = "INSERT INTO items (seller_id, title, content, price, trade_place, status, category, item_type, stock_quantity, view_count, chat_count, favorite_count, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?)";

        Category[] categories = Category.values();

        // 데이터 분포 설정
        int level1Limit = 50_000;     // 5만
        int level2Limit = 950_000;    // 90만 (누적)

        for (int i = 0; i < totalCount; i += batchSize) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                int currentIdx = i + j;

                Category category = categories[random.nextInt(categories.length)];
                String title = titleGenerator.generateTitle(category, currentIdx + 1);
                String content = titleGenerator.generateContent(category, title);
                int price = generatePriceByCategory(category);

                // 인덱스 구간에 따라 '동네'를 결정
                String targetNeighborhood;
                if (currentIdx < level1Limit) {
                    targetNeighborhood = LEVEL_1_NEIGHBORHOODS[random.nextInt(LEVEL_1_NEIGHBORHOODS.length)];
                } else if (currentIdx < level2Limit) {
                    targetNeighborhood = LEVEL_2_NEIGHBORHOODS[random.nextInt(LEVEL_2_NEIGHBORHOODS.length)];
                } else {
                    targetNeighborhood = LEVEL_3_NEIGHBORHOODS[random.nextInt(LEVEL_3_NEIGHBORHOODS.length)];
                }

                // [핵심 변경] 결정된 동네에 살고 있는 판매자의 ID를 가져옴
                Long sellerId = neighborhoodSellerMap.get(targetNeighborhood);

                batchArgs.add(new Object[]{
                        sellerId,           // 해당 동네 거주자의 ID
                        title,
                        content,
                        price,
                        targetNeighborhood, // trade_place (텍스트)
                        "ON_SALE",
                        category.name(),
                        "SALE",
                        1,
                        Timestamp.valueOf(LocalDateTime.now().minusMinutes(random.nextInt(500000))),
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
        String imageSql = """
            INSERT INTO item_images (item_id, image_url, is_representative, sort_order)
            SELECT item_id, 'mock.png', true, 0
            FROM items i
            WHERE NOT EXISTS (SELECT 1 FROM item_images img WHERE img.item_id = i.item_id)
        """;
        // 기존 seller_id = ? 조건 제거 (여러 판매자가 섞여 있으므로)
        jdbcTemplate.update(imageSql);

        System.out.println("Mock Data Generation Completed! (Total: " + totalCount + ")");
    }

    // 동네 목록을 받아 해당 동네에 사는 판매자를 생성하고 Map에 저장
    private void setupSellers(String[] neighborhoods) {
        String sql = "INSERT INTO members (username, password, nickname, phone_number, safety_score, role, is_location_verified, city, district, neighborhood, version, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, ?, 300, 'USER', 1, ?, ?, ?, 0, NOW(), NOW())";

        for (String fullAddress : neighborhoods) {
            // 이미 생성된 동네면 스킵
            if (neighborhoodSellerMap.containsKey(fullAddress)) continue;

            // 주소 파싱 ("경기도 평택시 중앙동" -> "경기도", "평택시", "중앙동")
            String[] parts = fullAddress.split(" ");
            String city = parts[0];
            String district = parts[1];
            String neighborhood = parts[2];

            // 유니크한 username 생성 (예: seller_중앙동)
            String username = "seller_" + neighborhood;
            String nickname = "판매자_" + neighborhood;
            String phoneNumber = "010-" + String.format("%04d", random.nextInt(9999)) + "-" + String.format("%04d", random.nextInt(9999));

            // DB에 존재하는지 확인
            List<Long> ids = jdbcTemplate.queryForList("SELECT member_id FROM members WHERE username = ?", Long.class, username);

            Long sellerId;
            if (!ids.isEmpty()) {
                sellerId = ids.get(0);
            } else {
                jdbcTemplate.update(sql,
                        username,
                        "$2a$10$gPrBypZokPwFnSKPptfhLeW0E45bhXAMmFztG74pgHUForE8A2dc.",
                        nickname,
                        phoneNumber,
                        city,
                        district,
                        neighborhood
                );
                sellerId = jdbcTemplate.queryForObject("SELECT last_insert_id()", Long.class);
            }

            // Map에 저장 (Key: "경기도 평택시 중앙동", Value: 15)
            neighborhoodSellerMap.put(fullAddress, sellerId);
        }
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