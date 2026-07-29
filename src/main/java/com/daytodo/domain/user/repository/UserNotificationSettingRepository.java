package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationSettingRepository
        extends JpaRepository<UserNotificationSetting, Long> {

    // User 엔티티의 실제 PK 필드명은 id이므로 연관 경로도 user.id로 지정한다.
    Optional<UserNotificationSetting> findByUser_Id(Long userId);
}
