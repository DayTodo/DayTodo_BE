package com.daytodo.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * TODO(팀 확인 필요): 실제 발송을 위해 build.gradle에 spring-boot-starter-mail 추가하고
 * application.yml에 spring.mail.host/port/username/password 설정이 필요합니다. (SMTP 계정 팀에서 준비)
 */
@Component
@RequiredArgsConstructor
public class AuthMailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[데이투두] 이메일 인증을 완료해주세요");
        message.setText("아래 링크(또는 토큰)로 이메일 인증을 완료해주세요.\n\n인증 토큰: " + token
                + "\n\nGET /auth/verify-email?token=" + token);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[데이투두] 비밀번호 재설정 인증 코드");
        message.setText("비밀번호 재설정 인증 코드: " + code + "\n10분 이내에 입력해주세요.");
        mailSender.send(message);
    }
}