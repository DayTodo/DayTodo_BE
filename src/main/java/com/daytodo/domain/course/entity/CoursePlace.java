package com.daytodo.domain.course.entity;

import com.daytodo.domain.common.BaseCreatedEntity;
import com.daytodo.domain.course.enums.CoursePlaceStatus;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "course_place")
public class CoursePlace extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_place_id")
    private Long coursePlaceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")
    private User addedBy;

    @Column(name = "place_order", nullable = false)
    private Integer placeOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_place_status", nullable = false, length = 20)
    private CoursePlaceStatus coursePlaceStatus;

    @Column(name = "visited_at")
    private LocalDateTime visitedAt;
}
