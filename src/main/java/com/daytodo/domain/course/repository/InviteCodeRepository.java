package com.daytodo.domain.course.repository;

import com.daytodo.domain.course.entity.InviteCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
    @EntityGraph(attributePaths = "course")
    Optional<InviteCode> findByCode(String code);

    boolean existsByCode(String code);
}
