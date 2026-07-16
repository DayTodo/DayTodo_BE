package com.daytodo.domain.course.entity;

import com.daytodo.domain.course.enums.CoursePlaceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "CoursePlace")
public class CoursePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_place_id")
    private Long coursePlaceId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "added_by", nullable = false)
    private Long addedBy;

    @Column(name = "place_order", nullable = false)
    private Integer placeOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_place_status", nullable = false, length = 20)
    private CoursePlaceStatus coursePlaceStatus;

    @Column(name = "visited_at")
    private LocalDateTime visitedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}