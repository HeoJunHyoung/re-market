package com.example.remarket.domain.region.service;

import com.example.remarket.domain.region.dto.RegionDto;
import com.example.remarket.domain.region.entity.NeighborhoodAdjacency;
import com.example.remarket.domain.region.repository.NeighborhoodAdjacencyRepository;
import com.example.remarket.global.util.GeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionService {

    private final NeighborhoodAdjacencyRepository adjacencyRepository;
    private final GeoService geoService;

    /**
     * CSV 파일 읽기 (5열 고정 포맷)
     * [0]: 시도, [1]: 시군구, [2]: 읍면동/구, [3]: 위도, [4]: 경도
     */
    public List<RegionDto> loadRegionsFromCsv() {
        List<RegionDto> regionList = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("gyeonggi_coordinates.csv");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // 헤더 스킵 (첫 줄 버림)

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                // 데이터 정합성 체크 (5개 열이 맞는지)
                if (data.length < 5) {
                    log.warn("잘못된 데이터 형식 패스: {}", line);
                    continue;
                }

                try {
                    // CSV 포맷에 맞춰 데이터 추출
                    String city = data[0].trim();
                    String district = data[1].trim();
                    String name = data[2].trim();

                    double lat = Double.parseDouble(data[3].trim()); // 4번째 열: 위도
                    double lng = Double.parseDouble(data[4].trim()); // 5번째 열: 경도

                    // 이름이 비어있지 않은 경우만 추가
                    if (!name.isEmpty()) {
                        regionList.add(new RegionDto(city, district, name, lat, lng));
                    }
                } catch (NumberFormatException e) {
                    log.warn("좌표 변환 실패 행: {}", line);
                }
            }
        } catch (Exception e) {
            log.error("CSV 파일 읽기 중 오류 발생", e);
        }
        return regionList;
    }

    /**
     * 거리 계산 및 DB 초기화 (배치성 로직)
     */
    @Transactional
    public void initRegionData() {
        // 1. CSV 로딩
        List<RegionDto> regions = loadRegionsFromCsv();
        log.info("로딩된 지역 데이터 수: {}개", regions.size());

        List<NeighborhoodAdjacency> adjacencyList = new ArrayList<>();

        // 2. N x N 거리 계산 루프
        for (RegionDto base : regions) {
            for (RegionDto target : regions) {

                if (base.getFullName().equals(target.getFullName())) {
                    adjacencyList.add(new NeighborhoodAdjacency(base.getFullName(), target.getFullName(), 0));
                    continue;
                }

                // GeoService 활용 거리 계산 (단위: km)
                double distance = geoService.calculateDistance(
                        base.getLat(), base.getLng(),
                        target.getLat(), target.getLng()
                );

                // 거리에 따른 레벨 분류
                int level = 0;
                if (distance <= 1.5) level = 1;      // 1.5km 이내 (동네 이웃)
                else if (distance <= 3.0) level = 2; // 3.0km 이내 (근처 이웃)
                else if (distance <= 5.0) level = 3; // 5.0km 이내 (먼 이웃)

                // 해당 범위 내에 들어오면 리스트에 추가
                if (level > 0) {
                    adjacencyList.add(new NeighborhoodAdjacency(base.getFullName(), target.getFullName(), level));
                }
            }
        }

        // 3. 기존 데이터 삭제 후 일괄 저장
        adjacencyRepository.deleteAll(); // 초기화
        adjacencyRepository.saveAll(adjacencyList);

        log.info("거리 계산 및 저장 완료! 총 데이터 수: {}건", adjacencyList.size());
    }

    /**
     * (추가) 실제 서비스에서 사용할 조회 메소드
     */
    @Transactional(readOnly = true)
    public List<String> getNearbyRegionNames(String baseRegion, int level) {
        return adjacencyRepository.findNearbyNeighborhoods(baseRegion, level);
    }
}