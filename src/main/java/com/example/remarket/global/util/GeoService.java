package com.example.remarket.global.util;

import com.example.remarket.domain.member.entity.Address;
import com.example.remarket.domain.region.exception.RegionErrorCode;
import com.example.remarket.global.error.BusinessException;
import com.example.remarket.global.error.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeoService {

    private final RestTemplate restTemplate;

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private static final String KAKAO_GEO_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

    /**
     * 위도/경도 좌표를 받아 행정구역 주소 객체(Address)로 반환
     */
    public Address getAddressFromCoordinates(Double latitude, Double longitude) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            // Kakao 로컬 API 호출 (x: 경도, y: 위도)
            String url = UriComponentsBuilder.fromHttpUrl(KAKAO_GEO_URL)
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .build()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            // 응답 파싱
            return parseAddressFromResponse(response.getBody());

        } catch (Exception e) {
            log.error("Kakao API 좌표 변환 실패 - lat: {}, lng: {}", latitude, longitude, e);
            throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR); // 또는 적절한 위치 에러 코드
        }
    }

    private Address parseAddressFromResponse(Map<String, Object> body) {
        if (body == null) return null;

        List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
        if (documents == null || documents.isEmpty()) {
            throw new BusinessException(RegionErrorCode.REGION_NOT_FOUND);
        }

        // Kakao API는 'B'(법정동)과 'H'(행정동) 정보 제공
        // 생활 서비스(당근 등)는 주로 '행정동(H)'을 기준
        for (Map<String, Object> doc : documents) {
            if ("H".equals(doc.get("region_type"))) {
                String city = (String) doc.get("region_1depth_name");      // 시/도 (예: 서울특별시)
                String district = (String) doc.get("region_2depth_name");  // 구/군 (예: 강남구)
                String neighborhood = (String) doc.get("region_3depth_name"); // 동/면/읍 (예: 역삼1동)

                return new Address(city, district, neighborhood);
            }
        }

        // 행정동 정보가 없다면 법정동(B) 정보라도 사용 (예외 처리)
        Map<String, Object> backupDoc = documents.get(0);
        String city = (String) backupDoc.get("region_1depth_name");
        String district = (String) backupDoc.get("region_2depth_name");
        String neighborhood = (String) backupDoc.get("region_3depth_name");

        return new Address(city, district, neighborhood);
    }

    /**
     * 하버사인(Haversine) 공식을 이용한 두 좌표 사이의 거리 계산 (단위: km)
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344; // 킬로미터 단위 변환
        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private String callKakaoApi(String url, Double lat, Double lng, String rootKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            String targetUrl = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("x", lng).queryParam("y", lat).build().toUriString();

            ResponseEntity<Map> response = restTemplate.exchange(targetUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.getBody().get(rootKey);
            for (Map<String, Object> doc : docs) {
                if ("H".equals(doc.get("region_type"))) return (String) doc.get("region_3depth_name");
            }
        } catch (Exception e) { log.error("API 호출 실패", e); }
        return null;
    }

}