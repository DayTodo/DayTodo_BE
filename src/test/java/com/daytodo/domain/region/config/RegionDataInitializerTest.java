package com.daytodo.domain.region.config;

import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RegionDataInitializerTest {
    private static final List<String> SEOUL_DISTRICTS = List.of(
            "종로구", "중구", "용산구", "성동구", "광진구",
            "동대문구", "중랑구", "성북구", "강북구", "도봉구",
            "노원구", "은평구", "서대문구", "마포구", "양천구",
            "강서구", "구로구", "금천구", "영등포구", "동작구",
            "관악구", "서초구", "강남구", "송파구", "강동구"
    );

    @Autowired
    RegionRepository regionRepository;

    @Test
    void seedsSeoulAndTwentyFiveDistricts() throws Exception {
        RegionDataInitializer initializer = new RegionDataInitializer(regionRepository);

        initializer.run(null);

        List<Region> regions = regionRepository.findAllByOrderByRegionIdAsc();
        List<Region> districts = regions.stream()
                .filter(region -> region.getRegionLevel() == RegionLevel.SIGUNGU)
                .toList();

        assertThat(regions).hasSize(26);
        assertThat(districts)
                .extracting(Region::getRegionName)
                .containsExactlyElementsOf(SEOUL_DISTRICTS);
        assertThat(districts)
                .allSatisfy(district -> assertThat(district.getParent().getRegionName())
                        .isEqualTo("서울특별시"));
    }

    @Test
    void doesNotInsertDuplicatesWhenRunAgain() throws Exception {
        RegionDataInitializer initializer = new RegionDataInitializer(regionRepository);

        initializer.run(null);
        initializer.run(null);

        assertThat(regionRepository.count()).isEqualTo(26);
        assertThat(regionRepository.findAllByOrderByRegionIdAsc().stream()
                .filter(region -> region.getRegionLevel() == RegionLevel.SIGUNGU)
                .map(Region::getRegionName))
                .containsExactlyElementsOf(SEOUL_DISTRICTS);
    }

    @Test
    void insertsOnlyMissingDistrictsWhenSomeDataAlreadyExists() throws Exception {
        Region seoul = regionRepository.save(new Region(null, "서울특별시", RegionLevel.SIDO));
        regionRepository.save(new Region(seoul, "종로구", RegionLevel.SIGUNGU));

        new RegionDataInitializer(regionRepository).run(null);

        List<Region> regions = regionRepository.findAllByOrderByRegionIdAsc();
        assertThat(regions).hasSize(26);
        assertThat(regions.stream()
                .filter(region -> "종로구".equals(region.getRegionName())))
                .hasSize(1);
        assertThat(regions.stream()
                .filter(region -> region.getRegionLevel() == RegionLevel.SIGUNGU)
                .map(Region::getRegionName))
                .containsExactlyInAnyOrderElementsOf(SEOUL_DISTRICTS);
    }
}
