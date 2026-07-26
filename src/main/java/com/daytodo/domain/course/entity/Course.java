package com.daytodo.domain.course.entity;

import com.daytodo.domain.common.BaseEntity;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.enums.RelationType;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.user.entity.User;
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
@Table(name = "course")
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "course_name", nullable = false, length = 20)
    private String courseName;

    @Column(name = "course_date", nullable = false)
    private LocalDate courseDate;

    @Column(name = "min_price", nullable = false)
    private Integer minPrice;

    @Column(name = "max_price", nullable = false)
    private Integer maxPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false, length = 20)
    private ParticipantType participantType;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", length = 20)
    private RelationType relationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status", nullable = false, length = 20)
    private CourseStatus courseStatus;

    @Column(name = "current_order_index")
    private Integer currentOrderIndex;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Course(
            User owner,
            Region region,
            String courseName,
            LocalDate courseDate,
            Integer minPrice,
            Integer maxPrice,
            ParticipantType participantType
    ) {
        this.owner = owner;
        this.region = region;
        this.courseName = courseName;
        this.courseDate = courseDate;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.participantType = participantType;
        this.courseStatus = CourseStatus.PLANNING;
    }

    public void resetRecommendationData() {
        //TODO : 추후 리셋 로직 추가
    }
}
