package com.daytodo.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스의 추억 사진
 *
 * 추억 사진은 코스 멤버 전원이 함께 보는 공용 사진이라(피그마 '기록' 화면에서 여러 멤버가
 * 같은 사진에 메모를 남기는 것으로 확인), course 단위로만 조회하고 diary와는 연관관계를
 * 맺지 않는다. 예전엔 diary_id로 "몇 번째 일기 작성 시점에 확정된 사진인지" 연결하려 했지만,
 * 일기를 먼저 쓴 뒤에 사진이 추가되는 순서도 API상 가능해서 diary_id가 영영 비어있는 사진이
 * 생길 수 있었다(PR #49 리뷰, 준열님 코멘트). 조회가 어차피 course 기준이라 diary_id는
 * 실질적으로 쓰이지 않았으므로, 그 원인 자체를 없애기 위해 연관관계를 제거했다.
 */
@Entity
@Table(
        name = "memory_photo",
        uniqueConstraints = {
                // 같은 코스 내 photo_order 중복 방지 (동시 업로드 시 순번 충돌을 DB에서 최종 차단)
                @UniqueConstraint(
                        name = "uk_memory_photo_course_id_photo_order",
                        columnNames = {"course_id", "photo_order"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemoryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memory_photo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "photo_order", nullable = false)
    private Integer photoOrder;

    @Builder
    public MemoryPhoto(Course course, String imageUrl, Integer photoOrder) {
        this.course = course;
        this.imageUrl = imageUrl;
        this.photoOrder = photoOrder;
    }
}