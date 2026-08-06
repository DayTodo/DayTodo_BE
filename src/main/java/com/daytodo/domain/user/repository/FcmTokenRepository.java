package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.FcmToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAllByUser_IdIn(Collection<Long> userIds);

    List<FcmToken> findAllByUser_Id(Long userId);

    long deleteByTokenAndUser_Id(String token, Long userId);
}
