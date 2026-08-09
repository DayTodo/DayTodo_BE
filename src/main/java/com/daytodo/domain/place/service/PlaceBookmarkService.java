package com.daytodo.domain.place.service;

import com.daytodo.domain.place.converter.BookmarkPlaceConverter;
import com.daytodo.domain.place.converter.MagazineConverter;
import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.entity.mapping.BookmarkPlace;
import com.daytodo.domain.place.enums.BookmarkSortType;
import com.daytodo.domain.place.exception.code.PlaceErrorCode;
import com.daytodo.domain.place.infra.TourApiClient;
import com.daytodo.domain.place.infra.TourApiResponse;
import com.daytodo.domain.place.repository.BookmarkPlaceRepository;
import com.daytodo.domain.place.repository.PlaceRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceBookmarkService {

    private final BookmarkPlaceRepository bookmarkPlaceRepository;
    private final PlaceRepository placeRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final TourApiClient tourApiClient;

    @Transactional(readOnly = true)
    public PlaceResDTO.GetBookmarkList getBookmarkList(
            Long userId,
            PlaceReqDTO.GetBookmarkList request
    ) {
        BookmarkSortType sort = (request.sort() == null) ? BookmarkSortType.RECENT : request.sort();
        Long regionId = request.regionId();

        List<BookmarkPlace> bookmarkPlaces = switch (sort) {
            case RECENT -> bookmarkPlaceRepository.findAllOrderByRecent(userId, regionId);
            case OLDEST -> bookmarkPlaceRepository.findAllOrderByOldest(userId, regionId);
            case NAME -> bookmarkPlaceRepository.findAllOrderByPlaceName(userId, regionId);
            case POPULAR -> bookmarkPlaceRepository.findAllOrderByPopular(userId, regionId);
        };

        return BookmarkPlaceConverter.toBookmarkList(bookmarkPlaces);
    }

    /**
     * 장소 저장(북마크). contentId 는 관광(KorService2) 콘텐츠 ID 이며, 내부 Place PK 가 아니다.
     * 자체 Place 가 없으면 detailCommon2 로 조회해 Place 를 만들어 넣는다(lazy upsert).
     */
    @Transactional
    public PlaceResDTO.CreateBookmark createBookmark(Long userId, Long contentId) {
        String tourContentId = String.valueOf(contentId);

        Place place = findOrCreatePlace(tourContentId);

        if (bookmarkPlaceRepository.existsByUser_IdAndPlace_PlaceId(userId, place.getPlaceId())) {
            throw new ProjectException(PlaceErrorCode.DUPLICATE_BOOKMARK);
        }

        User user = userRepository.getReferenceById(userId);
        BookmarkPlace saved;
        try {
            saved = bookmarkPlaceRepository.saveAndFlush(new BookmarkPlace(user, place));
        } catch (DataIntegrityViolationException e) {
            // 동시에 동일 요청이 들어와 유니크 제약(uk_bookmark_place_user_place)에 걸린 경우
            throw new ProjectException(PlaceErrorCode.DUPLICATE_BOOKMARK);
        }

        return PlaceResDTO.CreateBookmark.builder()
                .bookmarkId(saved.getId())
                .placeId(place.getPlaceId())
                .build();
    }
    /**
     * 장소 저장(북마크) - 내부 Place PK 기준. 코스/기록에서 다녀온 장소 저장용.
     * (매거진용 createBookmark(contentId)와 달리 TourAPI 조회 없이 기존 Place를 바로 사용)
     */
    @Transactional
    public PlaceResDTO.CreateBookmark createBookmarkByPlaceId(Long userId, Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ProjectException(PlaceErrorCode.PLACE_NOT_FOUND));

        if (bookmarkPlaceRepository.existsByUser_IdAndPlace_PlaceId(userId, placeId)) {
            throw new ProjectException(PlaceErrorCode.DUPLICATE_BOOKMARK);
        }

        User user = userRepository.getReferenceById(userId);
        BookmarkPlace saved;
        try {
            saved = bookmarkPlaceRepository.saveAndFlush(new BookmarkPlace(user, place));
        } catch (DataIntegrityViolationException e) {
            throw new ProjectException(PlaceErrorCode.DUPLICATE_BOOKMARK);
        }

        return PlaceResDTO.CreateBookmark.builder()
                .bookmarkId(saved.getId())
                .placeId(place.getPlaceId())
                .build();
    }

    /**
     * 관광 콘텐츠 ID 로 Place 를 조회하거나 없으면 생성한다.
     * 동시에 동일 contentId 로 생성 요청이 들어오면 tour_content_id 유니크 제약에 걸릴 수 있으므로,
     * DataIntegrityViolationException 발생 시 상대 트랜잭션이 저장한 Place 를 재조회한다.
     */
    private Place findOrCreatePlace(String tourContentId) {
        return placeRepository.findByTourContentId(tourContentId)
                .orElseGet(() -> {
                    try {
                        return placeRepository.saveAndFlush(createPlaceFromTour(tourContentId));
                    } catch (DataIntegrityViolationException e) {
                        return placeRepository.findByTourContentId(tourContentId)
                                .orElseThrow(() -> new ProjectException(PlaceErrorCode.TOUR_API_ERROR));
                    }
                });
    }

    /**
     * 장소 저장 해제. 본인 소유 북마크만 하드 삭제한다.
     */
    @Transactional
    public PlaceResDTO.DeleteBookmark deleteBookmark(Long userId, Long bookmarkId) {
        BookmarkPlace bookmark = bookmarkPlaceRepository.findByIdAndUser_Id(bookmarkId, userId)
                .orElseThrow(() -> new ProjectException(PlaceErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkPlaceRepository.delete(bookmark);

        return PlaceResDTO.DeleteBookmark.builder()
                .bookmarkId(bookmarkId)
                .deleted(true)
                .build();
    }

    // 관광 콘텐츠로부터 Place 생성. 존재하지 않는 contentId 면 MAGAZINE_NOT_FOUND.
    private Place createPlaceFromTour(String contentId) {
        TourApiResponse.CommonItem common = tourApiClient.detailCommon(contentId);
        if (common == null) {
            throw new ProjectException(PlaceErrorCode.MAGAZINE_NOT_FOUND);
        }

        Region region = resolveRegion(common.areacode(), common.sigungucode());
        String category = MagazineConverter.categoryName(common.contenttypeid());

        return Place.ofTourContent(
                contentId,
                region,
                nullToEmpty(common.title()),
                category == null ? "기타" : category,
                joinAddress(common.addr1(), common.addr2()),
                parseCoordinate(common.mapy()),   // 위도
                parseCoordinate(common.mapx()),   // 경도
                emptyToNull(common.tel()),
                firstImage(common)
        );
    }

    private Region resolveRegion(String areaCode, String sigunguCode) {
        Integer area = parseInt(areaCode);
        Integer sigungu = parseInt(sigunguCode);
        if (area == null || sigungu == null) {
            return null;
        }
        return regionRepository.findFirstByAreaCodeAndSigunguCode(area, sigungu).orElse(null);
    }

    private static String firstImage(TourApiResponse.CommonItem common) {
        if (common.firstimage() != null && !common.firstimage().isBlank()) {
            return common.firstimage();
        }
        return emptyToNull(common.firstimage2());
    }

    private static String joinAddress(String addr1, String addr2) {
        String base = addr1 == null ? "" : addr1.trim();
        if (addr2 != null && !addr2.isBlank()) {
            base = (base + " " + addr2.trim()).trim();
        }
        return base;
    }

    private static double parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
