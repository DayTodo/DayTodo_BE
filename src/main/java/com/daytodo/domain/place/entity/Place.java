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

    @Column (name = "naver_place_id", nullable = false, unique = true, length = 100)
    private String naverPlaceId;

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
