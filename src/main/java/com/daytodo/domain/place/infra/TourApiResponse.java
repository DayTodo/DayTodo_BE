package com.daytodo.domain.place.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 한국관광공사 KorService2 공통 응답 구조.
 * <pre>
 * { "response": { "header": {...}, "body": { "items": { "item": [ ... ] }, ... } } }
 * </pre>
 * 결과 0건일 때 items 가 "" (빈 문자열)로 오므로
 * application.yml 의 accept-empty-string-as-null-object=true 로 items=null 처리한다.
 * 필드명은 KorService2 JSON 키(소문자)와 일치해야 한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiResponse<T>(
        Response<T> response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response<T>(
            Header header,
            Body<T> body
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
            String resultCode,
            String resultMsg
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body<T>(
            Items<T> items,
            Integer numOfRows,
            Integer pageNo,
            Integer totalCount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items<T>(
            List<T> item
    ) {}

    /** items.item 리스트를 항상 non-null 로 꺼낸다. */
    public List<T> items() {
        if (response == null || response.body() == null || response.body().items() == null) {
            return List.of();
        }
        List<T> item = response.body().items().item();
        return item == null ? List.of() : item;
    }

    // ---- 엔드포인트별 item 레코드 ----

    /** areaBasedList2 (지역기반 관광정보) 목록 아이템 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AreaItem(
            String contentid,
            String contenttypeid,
            String title,
            String addr1,
            String addr2,
            String firstimage,
            String firstimage2,
            String areacode,
            String sigungucode,
            String tel,
            String cat1,
            String cat2,
            String cat3,
            String mapx,
            String mapy
    ) {}

    /** detailCommon2 (공통정보) 아이템 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommonItem(
            String contentid,
            String contenttypeid,
            String title,
            String tel,
            String homepage,
            String firstimage,
            String firstimage2,
            String areacode,
            String sigungucode,
            String addr1,
            String addr2,
            String zipcode,
            String mapx,
            String mapy,
            String overview,
            String cat1,
            String cat2,
            String cat3
    ) {}

    /** detailIntro2 (소개정보) 아이템 — contentTypeId 별 영업시간 필드가 다르다. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IntroItem(
            String contentid,
            String contenttypeid,
            // 음식점(39)
            String opentimefood,
            String restdatefood,
            // 관광지(12)
            String usetime,
            String restdate,
            // 문화시설(14)
            String usetimeculture,
            String restdateculture,
            // 레포츠(28)
            String usetimeleports,
            String restdateleports,
            // 쇼핑(38)
            String opentime,
            String restdateshopping
    ) {}

    /** detailImage2 (이미지정보) 아이템 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageItem(
            String contentid,
            String originimgurl,
            String smallimageurl,
            String imgname,
            String serialnum
    ) {}
}
