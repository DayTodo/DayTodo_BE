package com.daytodo.domain.place.entity;

import com.daytodo.domain.common.BaseEntity;
import com.daytodo.domain.place.enums.MagazineStatus;
import com.daytodo.domain.region.entity.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 경량 매거진 엔티티.
 * 콘텐츠 본문·사진은 KorService2(관광공사)에서 실시간으로 가져오고,
 * 이 테이블은 서비스가 덧붙이는 값(광고 여부·노출 상태·대표 지역)만 관리한다.
 * PK(magazine_id) 는 KorService2 의 관광 콘텐츠 ID(contentId) 를 그대로 사용한다.
 * (매거진 1개 = 관광 콘텐츠 1개라 자연키로 두어 자동증가 PK 를 쓰지 않는다.)
 * 사진이 하나도 없으면 클라이언트에서 디폴트 이미지로 대체한다(로컬 이미지 저장 안 함).
 */
@Getter
@Entity
@Table(name = "magazine")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Magazine extends BaseEntity {

    // 관광공사 contentId 를 매거진 PK 로 그대로 사용 (자동증가 아님, 수동 지정)
    @Id
    @Column(name = "magazine_id")
    private Long magazineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(name = "is_ad", nullable = false)
    private boolean isAd;

    @Enumerated(EnumType.STRING)
    @Column(name = "magazine_status", nullable = false, length = 20)
    private MagazineStatus magazineStatus;
}
