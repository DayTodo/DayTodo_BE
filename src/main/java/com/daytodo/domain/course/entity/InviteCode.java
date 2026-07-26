package com.daytodo.domain.course.entity;

import com.daytodo.domain.common.BaseCreatedEntity;
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
@Table(name = "invite_code")
public class InviteCode extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_code_id")
    private Long inviteCodeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public InviteCode(Course course, User creator, String code, LocalDateTime expiredAt) {
        this.course = course;
        this.creator = creator;
        this.code = code;
        this.expiredAt = expiredAt;
        this.isActive = true;
    }
}
