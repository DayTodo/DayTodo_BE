package com.daytodo.domain.course.entity;

import com.daytodo.domain.common.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invite_code")
public class InviteCode extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_code_id")
    private Long inviteCodeId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}