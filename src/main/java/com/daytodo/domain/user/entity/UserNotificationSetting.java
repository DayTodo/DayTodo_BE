package com.daytodo.domain.user.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_notification_setting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long id;

    /*
     * 사용자 한 명당 알림 설정 하나만 존재하므로
     * 일대일 관계로 설정합니다.
     */
    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    /*
     * Java boolean은 MySQL에서 보통 TINYINT(1)로 저장됩니다.
     *
     * false = 0
     * true  = 1
     */
    @Column(
            name = "push_enabled",
            nullable = false
    )
    private boolean pushEnabled;

    @Column(
            name = "course_d1_enabled",
            nullable = false
    )
    private boolean legacyCourseD1Enabled;

    @Column(
            name = "course_d0_enabled",
            nullable = false
    )
    private boolean legacyCourseD0Enabled;

    public UserNotificationSetting(User user) {
        this.user = user;
        this.pushEnabled = true;
        // 기존 DB의 NOT NULL 컬럼과 호환하기 위한 값이다. D-1/D-0는 개별 설정하지 않는다.
        this.legacyCourseD1Enabled = true;
        this.legacyCourseD0Enabled = true;
    }

    public void updatePushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }
}
