package com.daytodo.domain.region.service;

import com.daytodo.domain.region.dto.RegionResponse;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {
    private final RegionRepository regionRepository;

    public RegionResponse.Regions getRegions() {
        return new RegionResponse.Regions(
                regionRepository.findAllByOrderByRegionIdAsc().stream()
                        .map(this::toRegionItem)
                        .toList()
        );
    }

    private RegionResponse.RegionItem toRegionItem(Region region) {
        Region parent = region.getParent();
        return new RegionResponse.RegionItem(
                region.getRegionId(),
                region.getRegionName(),
                region.getRegionLevel(),
                parent == null ? null : parent.getRegionId(),
                parent == null ? null : parent.getRegionName()
        );
    }
}
