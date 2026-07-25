package com.daytodo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AuthMailService의 @Async 메서드(메일 발송)를 활성화하기 위한 설정.
 * 별도 Executor를 지정하지 않아 Spring Boot 기본 SimpleAsyncTaskExecutor를 사용한다.
 * TODO(팀 확인 필요): 트래픽이 커지면 스레드 풀 기반 TaskExecutor로 교체 검토.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}