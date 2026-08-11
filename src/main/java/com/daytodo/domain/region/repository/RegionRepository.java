package com.daytodo.domain.region.repository;

import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    @EntityGraph(attributePaths = "parent")
    List<Region> findAllByOrderByRegionIdAsc();

    @EntityGraph(attributePaths = "parent")
    List<Region> findAllByRegionIdIn(Collection<Long> regionIds);

    List<Region> findAllByParentIsNullAndRegionLevelAndRegionNameIn(
            RegionLevel regionLevel,
            Collection<String> regionNames
    );

    List<Region> findAllByParentInAndRegionLevel(
            Collection<Region> parents,
            RegionLevel regionLevel
    );

    // 관광 지역코드 -> Region 역매핑 (북마크 시 Place.region 세팅용). 미매핑 시 empty.
    Optional<Region> findFirstByAreaCodeAndSigunguCode(Integer areaCode, Integer sigunguCode);
}
