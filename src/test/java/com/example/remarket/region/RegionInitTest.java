package com.example.remarket.region;

import com.example.remarket.domain.region.service.RegionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
public class RegionInitTest {

    @Autowired
    private RegionService regionService;

    @Test
    @Rollback(false) // 테스트가 끝나도 DB 데이터를 유지함
    void initData() {
        System.out.println("=========== 거리 계산 시작 ===========");

        // 이 메서드가 실행되면서 CSV를 읽고 -> 계산하고 -> DB
        regionService.initRegionData();

        System.out.println("=========== 거리 계산 종료 ===========");
    }
}