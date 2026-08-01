package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserRequest;
import com.daytodo.domain.user.entity.Feedback;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.domain.user.repository.FeedbackRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock UserService userService;
    @Mock FeedbackRepository feedbackRepository;

    @Test
    void trimsAndStoresFeedbackOfAtLeastOneHundredCharacters() {
        User user = new User("user@example.com", "password", "daytodo", null, LoginType.LOCAL);
        when(userService.getActiveUser(1L)).thenReturn(user);
        String content = "가".repeat(100);

        new FeedbackService(userService, feedbackRepository)
                .submit(1L, new UserRequest.SubmitFeedback("  " + content + "  "));

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo(content);
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void rejectsTrimmedFeedbackShorterThanOneHundredCharacters() {
        User user = new User("user@example.com", "password", "daytodo", null, LoginType.LOCAL);
        when(userService.getActiveUser(1L)).thenReturn(user);

        assertThatThrownBy(() -> new FeedbackService(userService, feedbackRepository)
                .submit(1L, new UserRequest.SubmitFeedback(" " + "가".repeat(99) + " ")))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.FEEDBACK_TOO_SHORT);
        verify(feedbackRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
