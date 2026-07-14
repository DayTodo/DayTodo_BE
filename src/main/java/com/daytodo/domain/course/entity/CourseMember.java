package com.daytodo.domain.course.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CourseMember")
public class CourseMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_member_id")
    private Long courseMemberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "member_role", nullable = false, length = 20)
    private String memberRole;

    @Column(name = "member_status", nullable = false, length = 20)
    private String memberStatus;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}