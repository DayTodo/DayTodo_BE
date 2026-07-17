package com.daytodo.domain.auth.entity;

import com.daytodo.domain.common.BaseEntity;
import com.daytodo.domain.auth.enums.LoginType;
import com.daytodo.domain.auth.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // 소셜 로그인 전용 계정은 비밀번호가 없을 수 있음 -> nullable
    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    private UserStatus userStatus;

    // 탈퇴 시점 기록용 - 평소 NULL, 탈퇴 시 채워짐 (30일 후 배치 삭제 기준점)
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    public User(String email, String password, String nickname,
                String profileImageUrl, LoginType loginType, UserStatus userStatus) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.loginType = loginType;
        this.userStatus = userStatus;
    }

    // 탈퇴 처리
    public void withdraw() {
        this.userStatus = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    // 이메일 인증 완료 -> ACTIVE 전환
    public void activate() {
        this.userStatus = UserStatus.ACTIVE;
    }
}