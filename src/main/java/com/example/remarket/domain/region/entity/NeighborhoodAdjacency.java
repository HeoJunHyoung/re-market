package com.example.remarket.domain.region.entity;

import com.example.remarket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "neighborhood_adjacency", indexes = {
        // 검색 성능 최적화를 위한 복합 인덱스 (기준동네 + 거리레벨)
        @Index(name = "idx_base_level", columnList = "base_neighborhood, distance_level")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NeighborhoodAdjacency extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_neighborhood", nullable = false)
    private String baseNeighborhood;   // 기준 동네 (예: 장당동)

    @Column(name = "target_neighborhood", nullable = false)
    private String targetNeighborhood; // 인접 동네 (예: 이충동)

    @Column(name = "distance_level", nullable = false)
    private Integer distanceLevel;     // 거리 레벨 (1: 1.5km, 2: 3.0km, 3: 5.0km)

    public NeighborhoodAdjacency(String baseNeighborhood, String targetNeighborhood, Integer distanceLevel) {
        this.baseNeighborhood = baseNeighborhood;
        this.targetNeighborhood = targetNeighborhood;
        this.distanceLevel = distanceLevel;
    }
}