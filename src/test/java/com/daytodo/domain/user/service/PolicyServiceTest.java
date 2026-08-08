package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyServiceTest {

    @Test
    void readsBothPolicyDocumentsFromClasspath() {
        UserResponse.Policies policies = new PolicyService().getPolicies();

        assertThat(policies.termsOfService())
                .contains("데이투두(DayToDo) 이용약관")
                .contains("제20조 (준거법 및 관할법원)")
                .contains("3. 위치기반서비스 이용약관");
        assertThat(policies.privacyPolicy())
                .contains("데이투두(DayToDo) 개인정보처리방침")
                .contains("15) 개인정보 처리방침의 변경에 관한 사항")
                .contains("4. 마케팅/이벤트 정보 수신 동의 안내")
                .contains("6. 네이버 소셜 로그인 이용 시 안내");
    }
}
