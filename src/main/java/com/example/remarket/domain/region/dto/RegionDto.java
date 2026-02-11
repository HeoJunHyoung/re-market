package com.example.remarket.domain.region.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegionDto {
    private String name; // 동네 이름 (예: 장당동, 개군면)
    private double lat;  // 위도
    private double lng;  // 경도
}