package com.daytodo.domain.auth.repository;

import com.daytodo.domain.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
}