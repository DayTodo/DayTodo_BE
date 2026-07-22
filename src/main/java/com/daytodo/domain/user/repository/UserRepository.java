package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndUserStatus(Long id, UserStatus userStatus);

    List<User> findAllByUserStatusAndWithdrawnAtLessThanEqual(
            UserStatus userStatus,
            LocalDateTime withdrawnAt
    );
}
