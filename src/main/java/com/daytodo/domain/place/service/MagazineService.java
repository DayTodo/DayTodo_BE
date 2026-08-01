package com.daytodo.domain.place.service;

import com.daytodo.domain.place.converter.MagazineConverter;
import com.daytodo.domain.place.dto.response.MagazineResDTO;
import com.daytodo.domain.place.entity.Magazine;
import com.daytodo.domain.place.enums.MagazineStatus;
import com.daytodo.domain.place.exception.code.PlaceErrorCode;
import com.daytodo.domain.place.infra.TourApiClient;
import com.daytodo.domain.place.infra.TourApiResponse;
import com.daytodo.domain.place.repository.MagazineRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.entity.mapping.UserInterestRegion;
import com.daytodo.domain.user.repository.UserInterestRegionRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 매거진 조회 서비스.
 * 목록은 관심지역 기준 areaBasedList2 로 조회하고, Magazine 테이블로 광고 여부를 오버레이한다.
 * 상세/사진은 KorService2 를 실시간 프록시한다.
 */
@Service
@RequiredArgsConstructor
public class MagazineService {

    private static final int LIST_SIZE = 10; // tagline(overview) N+1 비용 때문에 작게 유지

    private final TourApiClient tourApiClient;
    private final MagazineRepository magazineRepository;
    private final RegionRepository regionRepository;
    private final UserInterestRegionRepository userInterestRegionRepository;

    public MagazineResDTO.GetMagazineList getMagazineList(Long userId) {
        Region region = resolveInterestRegion(userId);
        Integer areaCode = region == null ? null : region.getAreaCode();
        Integer sigunguCode = region == null ? null : region.getSigunguCode();

        List<TourApiResponse.AreaItem> items = tourApiClient.areaBasedList(areaCode, sigunguCode, LIST_SIZE);
        if (items.isEmpty()) {
            return new MagazineResDTO.GetMagazineList(List.of());
        }

        Map<String, Boolean> adFlags = loadAdFlags(items);

        List<MagazineResDTO.GetMagazineList.MagazineItem> magazines = items.stream()
                .map(item -> {
                    boolean isAd = adFlags.getOrDefault(item.contentid(), false);
                    String tagline = fetchOverview(item.contentid());
                    return MagazineConverter.toMagazineItem(item, isAd, tagline);
                })
                // 광고 우선 정렬 (그 외 순서는 areaBasedList2 정렬 유지)
                .sorted((a, b) -> Boolean.compare(b.isAd(), a.isAd()))
                .toList();

        return new MagazineResDTO.GetMagazineList(magazines);
    }

    public MagazineResDTO.GetMagazineDetail getMagazineDetail(Long magazineId) {
        String contentId = String.valueOf(magazineId);

        TourApiResponse.CommonItem common = tourApiClient.detailCommon(contentId);
        if (common == null) {
            throw new ProjectException(PlaceErrorCode.MAGAZINE_NOT_FOUND);
        }

        TourApiResponse.IntroItem intro = tourApiClient.detailIntro(contentId, common.contenttypeid());
        List<TourApiResponse.ImageItem> images = tourApiClient.detailImage(contentId);

        return MagazineConverter.toDetail(common, intro, images);
    }

    public MagazineResDTO.GetMagazinePhotos getMagazinePhotos(Long magazineId) {
        String contentId = String.valueOf(magazineId);

        List<TourApiResponse.ImageItem> images = tourApiClient.detailImage(contentId);
        if (images.isEmpty() && tourApiClient.detailCommon(contentId) == null) {
            throw new ProjectException(PlaceErrorCode.MAGAZINE_NOT_FOUND);
        }
        return MagazineConverter.toPhotos(images);
    }

    // 관심지역 중 관광 지역코드가 매핑된 첫 지역을 사용. 없으면 전국(null).
    private Region resolveInterestRegion(Long userId) {
        List<Long> regionIds = userInterestRegionRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(UserInterestRegion::getRegionId)
                .toList();
        if (regionIds.isEmpty()) {
            return null;
        }
        return regionRepository.findAllByRegionIdIn(regionIds).stream()
                .filter(r -> r.getAreaCode() != null)
                .findFirst()
                .orElse(null);
    }

    // 노출 상태인 매거진만 조회해 magazineId(=contentId, String) -> isAd 로 매핑.
    // DB 조회는 Long(BIGINT)으로 하되, 목록 매칭 편의를 위해 map 키는 String 으로 유지한다.
    private Map<String, Boolean> loadAdFlags(List<TourApiResponse.AreaItem> items) {
        List<Long> magazineIds = items.stream()
                .map(TourApiResponse.AreaItem::contentid)
                .map(MagazineService::parseContentId)
                .filter(id -> id != null)
                .toList();
        if (magazineIds.isEmpty()) {
            return Map.of();
        }
        return magazineRepository.findByMagazineIdInAndMagazineStatus(magazineIds, MagazineStatus.EXPOSED).stream()
                .collect(Collectors.toMap(m -> String.valueOf(m.getMagazineId()), Magazine::isAd, (a, b) -> a || b));
    }

    // KorService2 contentId(숫자 문자열) -> Long. 비정상 값은 null 로 흡수.
    private static Long parseContentId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 목록 tagline 용 overview. 실패해도 목록 전체가 죽지 않도록 null 로 흡수.
    private String fetchOverview(String contentId) {
        try {
            return Optional.ofNullable(tourApiClient.detailCommon(contentId))
                    .map(TourApiResponse.CommonItem::overview)
                    .orElse(null);
        } catch (ProjectException e) {
            return null;
        }
    }
}
