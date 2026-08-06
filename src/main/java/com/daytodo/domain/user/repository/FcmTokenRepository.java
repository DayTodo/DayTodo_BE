package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.FcmToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FcmToken> findByToken(String token);

    @Modifying
    @Query(value = """
            insert into fcm_token (user_id, token, platform, created_at, updated_at)
            values (:userId, :token, :platform, current_timestamp, current_timestamp)
            on duplicate key update
                user_id = values(user_id),
                platform = values(platform),
                updated_at = current_timestamp
            """, nativeQuery = true)
    void upsert(
            @Param("userId") Long userId,
            @Param("token") String token,
            @Param("platform") String platform
    );

    List<FcmToken> findAllByUser_IdIn(Collection<Long> userIds);

    List<FcmToken> findAllByUser_Id(Long userId);

    long deleteByTokenAndUser_Id(String token, Long userId);
}
