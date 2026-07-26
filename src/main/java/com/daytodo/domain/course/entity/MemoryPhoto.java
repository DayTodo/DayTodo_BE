package com.daytodo.domain.course.entity;

import com.daytodo.domain.common.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ERD(보라색 테이블)에는 있었지만 어떤 팀원도 아직 만들지 않았던 엔티티입니다.
 * TDY-00(투데이 추억 사진 저장, 준열님 파트로 추정)과 이 엔티티를 같이 쓰게 될 수 있어서
 * 필드/제약조건은 팀과 한번 맞춰보는 게 좋습니다.
 */
@Entity
@Table(
        name = "memory_photo",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_memory_photo_diary_id_photo_order",
                        columnNames = {"diary_id", "photo_order"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemoryPhoto extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memory_photo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "photo_order", nullable = false)
    private Integer photoOrder;

    @Builder
    public MemoryPhoto(Diary diary, String imageUrl, Integer photoOrder) {
        this.diary = diary;
        this.imageUrl = imageUrl;
        this.photoOrder = photoOrder;
    }
}