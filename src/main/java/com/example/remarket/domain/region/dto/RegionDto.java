package com.example.remarket.domain.region.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegionDto {
    private String city;     // 시/도 (예: 경기도)
    private String district; // 시/군/구 (예: 평택시)
    private String name;     // 읍/면/동 (예: 중앙동)

    private double lat;      // 위도
    private double lng;      // 경도

    public String getFullName() {
        return city + " " + district + " " + name;
    }
}