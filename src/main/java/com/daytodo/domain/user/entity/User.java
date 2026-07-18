package com.daytodo.domain.user.entity;

import com.daytodo.domain.common.BaseEntity;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(
            name = "email",
            length = 100,
            nullable = false,
            unique = true
    )
    private String email;

    @Column(
            name = "password",
            length = 255
    )
    private String password;

    @Column(
            name = "nickname",
            length = 20,
            nullable = false
    )
    private String nickname;

    @Column(
            name = "profile_image_url",
            length = 500
    )
    private String profileImageUrl;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "login_type",
            length = 20,
            nullable = false
    )
    private LoginType loginType;


    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "user_status",
            length = 20,
            nullable = false
    )
    private UserStatus userStatus;

    public User(
            String email,
            String password,
            String nickname,
            String profileImageUrl,
            LoginType loginType
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.loginType = loginType;
        this.userStatus = UserStatus.ACTIVE;
    }

    public void updateProfile(
            String nickname,
            String profileImageUrl
    ) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }


}
