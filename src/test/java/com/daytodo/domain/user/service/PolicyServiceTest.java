package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyServiceTest {

    @Test
    void readsBothPolicyDocumentsFromClasspath() {
        UserResponse.Policies policies = new PolicyService().getPolicies();

        assertThat(policies.termsOfService()).contains("DayTodo 이용약관");
        assertThat(policies.privacyPolicy()).contains("DayTodo 개인정보처리방침");
    }
}
