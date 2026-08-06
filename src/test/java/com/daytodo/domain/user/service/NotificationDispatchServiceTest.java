package com.daytodo.domain.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.TransactionDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDispatchServiceTest {

    @Test
    void dispatchDoesNotExposeReadOnlyTransactionToDeliveryWorker() throws Exception {
        var method = NotificationDispatchService.class.getMethod("dispatchRetryable");
        var attribute = new AnnotationTransactionAttributeSource()
                .getTransactionAttribute(method, NotificationDispatchService.class);

        assertThat(attribute).isNotNull();
        assertThat(attribute.getPropagationBehavior())
                .isEqualTo(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
    }
}
