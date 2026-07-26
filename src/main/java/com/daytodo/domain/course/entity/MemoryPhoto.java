package com.daytodo.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스의 추억 사진
 *
 * TODO(팀 확인 필요): PR #21(auth-diary-api)에도 같은 이름의 엔티티가 있습니다.
 * 그쪽은 diary_id 가 NOT NULL 이지만, ERD와 TDY-008 명세는
 * "사진 저장 시점에는 diary_id 를 비워두고 이후 일기 작성 시 연결"이므로 nullable 로 두었습니다.
 * ERD에 created_at 이 없어 BaseCreatedEntity 도 상속하지 않았습니다.
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

    // 사진 저장 시점에는 비워두고, 해당 날짜의 일기가 작성될 때 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

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

    // 일기 작성 시 해당 일기에 연결
    public void linkDiary(Diary diary) {
        this.diary = diary;
    }
}
