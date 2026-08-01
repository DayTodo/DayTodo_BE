package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.entity.Feedback;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.FeedbackRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final int MIN_CONTENT_LENGTH = 100;

    private final UserService userService;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void submit(Long userId, UserRequest.SubmitFeedback request) {
        User user = userService.getActiveUser(userId);
        String content = request.content().trim();
        if (content.length() < MIN_CONTENT_LENGTH) {
            throw new ProjectException(UserErrorCode.FEEDBACK_TOO_SHORT);
        }
        feedbackRepository.save(new Feedback(user, content));
    }
}
