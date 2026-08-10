package com.daytodo.domain.region.service;

import com.daytodo.domain.region.dto.RegionResponse;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {
    @Mock RegionRepository regionRepository;
    @InjectMocks RegionService regionService;

    @Test
    void returnsAllRegionsWithHierarchy() {
        Region seoul = region(1L, null, "서울특별시", RegionLevel.SIDO);
        Region gangnam = region(2L, seoul, "강남구", RegionLevel.SIGUNGU);
        when(regionRepository.findAllByOrderByRegionIdAsc()).thenReturn(List.of(seoul, gangnam));

        RegionResponse.Regions response = regionService.getRegions();

        assertThat(response.regions()).containsExactly(
                new RegionResponse.RegionItem(1L, "서울특별시", RegionLevel.SIDO, null, null),
                new RegionResponse.RegionItem(2L, "강남구", RegionLevel.SIGUNGU, 1L, "서울특별시")
        );
    }

    @Test
    void returnsEmptyListWhenNoRegionsExist() {
        when(regionRepository.findAllByOrderByRegionIdAsc()).thenReturn(List.of());

        assertThat(regionService.getRegions().regions()).isEmpty();
    }

    private Region region(Long id, Region parent, String name, RegionLevel level) {
        Region region = new Region(parent, name, level);
        ReflectionTestUtils.setField(region, "regionId", id);
        return region;
    }
}
