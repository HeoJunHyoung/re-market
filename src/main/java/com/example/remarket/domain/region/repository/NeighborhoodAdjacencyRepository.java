package com.example.remarket.domain.region.repository;

import com.example.remarket.domain.region.entity.NeighborhoodAdjacency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NeighborhoodAdjacencyRepository extends JpaRepository<NeighborhoodAdjacency, Long> {

    // 특정 동네(base)를 기준으로 특정 레벨(level) 이하인 모든 인접 동네 이름 조회
    @Query("SELECT n.targetNeighborhood FROM NeighborhoodAdjacency n " +
            "WHERE n.baseNeighborhood = :baseNeighborhood " +
            "AND n.distanceLevel <= :level")
    List<String> findNearbyNeighborhoods(@Param("baseNeighborhood") String baseNeighborhood,
                                         @Param("level") int level);
}