package com.daytodo.domain.region.dto;

import com.daytodo.domain.region.enums.RegionLevel;

import java.util.List;

public final class RegionResponse {
    private RegionResponse() {
    }

    public record RegionItem(
            Long regionId,
            String regionName,
            RegionLevel regionLevel,
            Long parentRegionId,
            String parentRegionName
    ) {
    }

    public record Regions(List<RegionItem> regions) {
        public Regions {
            regions = List.copyOf(regions);
        }
    }
}
