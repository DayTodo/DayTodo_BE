package com.daytodo.domain.place.dto.response;

import lombok.Builder;

import java.util.List;

public class MagazineResDTO {

    // 오늘의 Pick 매거진 목록 (HOM-004)
    public record GetMagazineList(
            List<MagazineItem> magazines
    ) {
        @Builder
        public record MagazineItem(
                Long magazineId,     // 매거진 PK = 관광공사 contentId
                String thumbnailUrl,
                String placeName,
                String regionName,
                String tagline,      // overview 로 대체
                boolean isAd
        ) {}
    }

    // 매거진 상세
    @Builder
    public record GetMagazineDetail(
            Long magazineId,         // 매거진 PK = 관광공사 contentId (= placeId)
            Long placeId,
            String thumbnailUrl,
            String category,
            String placeName,
            String address,
            String businessHours,
            String phone,
            String content,          // overview 로 대체
            List<PhotoItem> photos
    ) {}

    // 매거진 사진 목록 (MAG-003)
    public record GetMagazinePhotos(
            List<PhotoItem> photos
    ) {}

    @Builder
    public record PhotoItem(
            String imageId,          // KorService2 serialnum (예: "2871004_3", 정수 아님)
            String imageUrl,
            Integer imageOrder       // 응답 순서 기반 1부터
    ) {}
}
