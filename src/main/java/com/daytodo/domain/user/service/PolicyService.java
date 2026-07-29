package com.daytodo.domain.user.service;

import com.daytodo.domain.user.dto.UserResponse;
import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class PolicyService {

    private static final String TERMS_PATH = "policies/terms-of-service.txt";
    private static final String PRIVACY_PATH = "policies/privacy-policy.txt";

    public UserResponse.Policies getPolicies() {
        return new UserResponse.Policies(read(TERMS_PATH), read(PRIVACY_PATH));
    }

    private String read(String path) {
        try {
            return new ClassPathResource(path)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ProjectException(UserErrorCode.POLICY_NOT_AVAILABLE);
        }
    }
}
