package com.daytodo.domain.course.entity;

import com.daytodo.domain.common.BaseEntity;
import com.daytodo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "diary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false) // DIA-001: 완료된 코스 기준 작성 -> NOT NULL 확정
    private Course course;

    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    @Column(length = 1000)
    private String content; // 코스 종료 시 자동 생성, 이후 사용자가 작성 -> nullable

    @Builder
    public Diary(User user, Course course, LocalDate diaryDate, String content) {
        this.user = user;
        this.course = course;
        this.diaryDate = diaryDate;
        this.content = content;
    }

    // 일기 내용 작성/수정
    public void writeContent(String content) {
        this.content = content;
    }
}
