package com.daytodo.domain.user.storage;

import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3ProfileImageStorageTest {

    private final S3ProfileImageStorage storage = new S3ProfileImageStorage();

    @Test
    void rejectsFileLargerThanFiveMegabytes() {
        byte[] oversized = new byte[5 * 1024 * 1024 + 1];
        oversized[0] = (byte) 0xFF;
        oversized[1] = (byte) 0xD8;
        oversized[2] = (byte) 0xFF;

        assertError(
                new MockMultipartFile("profileImage", "large.jpg", "image/jpeg", oversized),
                UserErrorCode.PROFILE_IMAGE_TOO_LARGE
        );
    }

    @Test
    void rejectsContentWhoseSignatureDoesNotMatchJpegOrPng() {
        assertError(
                new MockMultipartFile(
                        "profileImage",
                        "fake.jpg",
                        "image/jpeg",
                        "not-an-image".getBytes(java.nio.charset.StandardCharsets.UTF_8)
                ),
                UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT
        );
    }

    @Test
    void rejectsMismatchedContentTypeAndPngSignature() {
        byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertError(
                new MockMultipartFile("profileImage", "image.jpg", "image/jpeg", png),
                UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT
        );
    }

    @Test
    void validImageReportsMissingS3ConfigurationOnlyAtUploadTime() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        assertError(
                new MockMultipartFile("profileImage", "image.jpg", "image/jpeg", jpeg),
                UserErrorCode.PROFILE_IMAGE_STORAGE_NOT_CONFIGURED
        );
    }

    private void assertError(MockMultipartFile file, UserErrorCode errorCode) {
        assertThatThrownBy(() -> storage.upload(1L, file))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
