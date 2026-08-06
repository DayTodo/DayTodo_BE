package com.daytodo.domain.user.push;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledPushNotificationGateway implements PushNotificationGateway {

    @Override
    public PushResult send(String token, String title, String body, Map<String, String> data) {
        return PushResult.failed(false, "Firebase Cloud Messaging이 설정되지 않았습니다.");
    }
}
