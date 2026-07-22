package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.mapping.UserInterestRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRegionRepository extends JpaRepository<UserInterestRegion, Long> {
    List<UserInterestRegion> findAllByUserIdOrderByIdAsc(Long userId);

    @Modifying
    @Query("delete from UserInterestRegion interest where interest.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
