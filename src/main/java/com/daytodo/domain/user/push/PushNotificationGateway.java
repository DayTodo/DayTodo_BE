package com.daytodo.domain.user.push;

import java.util.Map;

public interface PushNotificationGateway {

    PushResult send(String token, String title, String body, Map<String, String> data);

    record PushResult(boolean success, boolean invalidToken, String errorMessage) {
        public static PushResult sent() {
            return new PushResult(true, false, null);
        }

        public static PushResult failed(boolean invalidToken, String errorMessage) {
            return new PushResult(false, invalidToken, errorMessage);
        }
    }
}
