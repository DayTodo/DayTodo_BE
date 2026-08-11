package com.daytodo.domain.region.config;

import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RegionDataInitializer implements ApplicationRunner {
    private static final String SEOUL = "서울특별시";
    private static final List<String> SEOUL_NAMES = List.of(SEOUL, "서울");
    private static final List<String> SEOUL_DISTRICTS = List.of(
            "종로구", "중구", "용산구", "성동구", "광진구",
            "동대문구", "중랑구", "성북구", "강북구", "도봉구",
            "노원구", "은평구", "서대문구", "마포구", "양천구",
            "강서구", "구로구", "금천구", "영등포구", "동작구",
            "관악구", "서초구", "강남구", "송파구", "강동구"
    );

    private final RegionRepository regionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Region> existingSeoulRegions = regionRepository
                .findAllByParentIsNullAndRegionLevelAndRegionNameIn(RegionLevel.SIDO, SEOUL_NAMES);

        Region seoul = existingSeoulRegions.stream()
                .filter(region -> SEOUL.equals(region.getRegionName()))
                .findFirst()
                .orElseGet(() -> existingSeoulRegions.stream().findFirst()
                        .orElseGet(() -> regionRepository.save(
                                new Region(null, SEOUL, RegionLevel.SIDO)
                        )));

        List<Region> seoulRegions = existingSeoulRegions.isEmpty()
                ? List.of(seoul)
                : existingSeoulRegions;

        Set<String> existingDistrictNames = regionRepository
                .findAllByParentInAndRegionLevel(seoulRegions, RegionLevel.SIGUNGU)
                .stream()
                .map(Region::getRegionName)
                .collect(Collectors.toSet());

        List<Region> missingDistricts = SEOUL_DISTRICTS.stream()
                .filter(name -> !existingDistrictNames.contains(name))
                .map(name -> new Region(seoul, name, RegionLevel.SIGUNGU))
                .toList();

        if (!missingDistricts.isEmpty()) {
            regionRepository.saveAll(missingDistricts);
        }
    }
}
