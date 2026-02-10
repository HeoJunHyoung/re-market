package com.example.remarket.global.util;

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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private static final String KAKAO_GEO_URL = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

    /**
     * 현재 좌표가 속한 동네 이름 가져오기 (기존 기능)
     */
    public String getRegionName(Double latitude, Double longitude) {
        String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";
        return callKakaoApi(url, latitude, longitude, "documents");
    }

    /**
     * 주소를 위도/경도 좌표로 변환하기
     */
    public Map<String, Double> getCoordinates(String address) {
        try {
            String url = "https://dapi.kakao.com/v2/local/search/address.json";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            String targetUrl = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("query", address)
                    .build().toUriString();

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(targetUrl, HttpMethod.GET, entity, Map.class);

            List<Map<String, Object>> documents = (List<Map<String, Object>>) response.getBody().get("documents");
            if (!documents.isEmpty()) {
                Map<String, Object> first = documents.get(0);
                return Map.of(
                        "lat", Double.parseDouble(first.get("y").toString()),
                        "lng", Double.parseDouble(first.get("x").toString())
                );
            }
        } catch (Exception e) {
            log.error("주소 좌표 변환 실패: {}", address, e);
        }
        return null;
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

    private double deg2rad(double deg) { return (deg * Math.PI / 180.0); }
    private double rad2deg(double rad) { return (rad * 180.0 / Math.PI); }

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