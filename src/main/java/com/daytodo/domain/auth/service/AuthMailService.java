package com.daytodo.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * TODO(팀 확인 필요): 실제 발송을 위해 build.gradle에 spring-boot-starter-mail 추가하고
 * application.yml에 spring.mail.host/port/username/password 설정이 필요합니다. (SMTP 계정 팀에서 준비)
 *
 * @Async로 처리해서 회원가입/비밀번호 재설정 등의 DB 트랜잭션과 메일 발송을 분리했다.
 * SMTP가 느리거나 실패해도 이미 커밋된 회원가입/토큰 저장까지 롤백되지 않도록 하기 위함.
 * (AsyncConfig의 @EnableAsync 필요, 예외는 발송 실패로만 로그 남기고 흡수)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[데이투두] 이메일 인증을 완료해주세요");
        message.setText("아래 링크(또는 토큰)로 이메일 인증을 완료해주세요.\n\n인증 토큰: " + token
                + "\n\nGET /auth/verify-email?token=" + token);
        sendSafely(message);
    }

    @Async
    public void sendPasswordResetEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[데이투두] 비밀번호 재설정 인증 코드");
        message.setText("비밀번호 재설정 인증 코드: " + code + "\n10분 이내에 입력해주세요.");
        sendSafely(message);
    }

    private void sendSafely(SimpleMailMessage message) {
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            // 비동기로 분리되어 있으므로 여기서 실패해도 이미 커밋된 트랜잭션에는 영향 없음.
            // TODO(팀 확인 필요): 발송 실패 시 재시도 큐/알림 등 후속 처리 정책 필요.
            log.error("메일 발송 실패: to={}", maskedRecipient(message), exception);
        }
    }

    private String maskedRecipient(SimpleMailMessage message) {
        String[] recipients = message.getTo();
        if (recipients == null || recipients.length == 0
                || recipients[0] == null || recipients[0].isBlank()) {
            return "unknown";
        }
        return maskEmail(recipients[0]);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            // '@'가 없거나 맨 앞에 있는 등 정상적인 이메일 형식이 아니면 전부 마스킹 처리한다.
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}