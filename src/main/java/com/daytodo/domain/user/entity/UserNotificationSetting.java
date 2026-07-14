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
    private boolean courseD1Enabled;

    @Column(
            name = "course_d0_enabled",
            nullable = false
    )
    private boolean courseD0Enabled;

    public UserNotificationSetting(User user) {
        this.user = user;
        this.pushEnabled = true;
        this.courseD1Enabled = true;
        this.courseD0Enabled = true;
    }

    public void updateSettings(
            boolean pushEnabled,
            boolean courseD1Enabled,
            boolean courseD0Enabled
    ) {
        this.pushEnabled = pushEnabled;
        this.courseD1Enabled = courseD1Enabled;
        this.courseD0Enabled = courseD0Enabled;
    }
}
