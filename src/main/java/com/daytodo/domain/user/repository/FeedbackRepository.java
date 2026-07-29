package com.daytodo.domain.user.repository;

import com.daytodo.domain.user.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
