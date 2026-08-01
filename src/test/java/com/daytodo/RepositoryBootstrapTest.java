package com.daytodo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class RepositoryBootstrapTest {

    // 기존 테스트 환경의 JavaMailSender 미설정 문제만 격리하고 전체 Repository 생성을 검증한다.
    @MockitoBean
    JavaMailSender javaMailSender;

    @Test
    void applicationContextCreatesAllRepositoryQueries() {
    }
}
