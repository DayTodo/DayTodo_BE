package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndUserStatus(Long id, UserStatus userStatus);

    List<User> findAllByUserStatusAndWithdrawnAtLessThanEqual(
            UserStatus userStatus,
            LocalDateTime withdrawnAt
    );

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNicknameAndIdNot(String nickname, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select user from User user
            where user.id = :userId
              and user.userStatus = :userStatus
            """)
    Optional<User> findActiveUserForUpdate(
            @Param("userId") Long userId,
            @Param("userStatus") UserStatus userStatus
    );
}
