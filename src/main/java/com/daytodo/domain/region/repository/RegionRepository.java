package com.daytodo.domain.region.repository;

import com.daytodo.domain.region.entity.Region;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {
    @EntityGraph(attributePaths = "parent")
    List<Region> findAllByRegionIdIn(Collection<Long> regionIds);
}
