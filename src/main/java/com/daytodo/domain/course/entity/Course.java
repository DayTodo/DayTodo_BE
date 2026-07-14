package com.daytodo.domain.course.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "course_name", nullable = false, length = 20)
    private String courseName;

    @Column(name = "course_date", nullable = false)
    private LocalDate courseDate;

    @Column(name = "min_price", nullable = false)
    private Integer minPrice;

    @Column(name = "max_price", nullable = false)
    private Integer maxPrice;

    @Column(name = "participant_type", nullable = false, length = 20)
    private String participantType;

    @Column(name = "relation_type", nullable = false, length = 20)
    private String relationType;

    @Column(name = "course_status", nullable = false, length = 20)
    private String courseStatus;

    @Column(name = "current_order_index")
    private Integer currentOrderIndex;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}