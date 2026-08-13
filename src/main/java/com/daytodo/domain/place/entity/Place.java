package com.daytodo.domain.place.entity;

import com.daytodo.domain.common.BaseEntity;
import com.daytodo.domain.region.entity.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Place extends BaseEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column (name = "place_id",  nullable = false)
    private Long placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    // Naver 출처 장소 식별자. 관광(KorService2) 출처 장소는 값이 없으므로 nullable.
    @Column (name = "naver_place_id", unique = true, length = 100)
    private String naverPlaceId;

    // 한국관광공사 KorService2 콘텐츠 ID. 관광 출처 장소 식별자.
    @Column (name = "tour_content_id", unique = true, length = 20)
    private String tourContentId;

    @Column (name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column (name = "category", nullable = false, length = 100)
    private String category;

    @Column (name = "address", nullable = false, length = 255)
    private String address;

    @Column (name = "road_address", length = 255)
    private String roadAddress;

    @Column (name = "latitude", nullable = false)
    private double latitude;

    @Column (name = "longitude", nullable = false)
    private double longitude;

    @Column (name = "phone", length = 30)
    private String phone;

    @Column (name = "description", length = 500)
    private String description;

    @Column (name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 관광(KorService2) 콘텐츠로부터 Place 를 생성한다 (북마크 시 lazy upsert 용).
     * region 은 관광 지역코드 역매핑 결과이며, 미매핑 시 null 이다.
     */
    public static Place ofTourContent(
            String tourContentId,
            Region region,
            String placeName,
            String category,
            String address,
            double latitude,
            double longitude,
            String phone,
            String description,
            String imageUrl
    ) {
        Place place = new Place();
        place.tourContentId = tourContentId;
        place.region = region;
        place.placeName = placeName;
        place.category = category;
        place.address = address;
        place.latitude = latitude;
        place.longitude = longitude;
        place.phone = phone;
        place.description = description;
        place.imageUrl = imageUrl;
        return place;
    }
  
    public Place(Region region, String naverPlaceId, String placeName, String category,
                 String address, String roadAddress, double latitude, double longitude,
                 String phone, String description, String imageUrl) {
        this.region = region;
        this.naverPlaceId = naverPlaceId;
        this.placeName = placeName;
        this.category = category;
        this.address = address;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
