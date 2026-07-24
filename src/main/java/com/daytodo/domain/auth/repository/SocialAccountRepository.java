package com.daytodo.domain.auth.repository;

import com.daytodo.domain.auth.entity.SocialAccount;
import com.daytodo.domain.auth.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    boolean existsByUserIdAndProvider(Long userId, SocialProvider provider);
}