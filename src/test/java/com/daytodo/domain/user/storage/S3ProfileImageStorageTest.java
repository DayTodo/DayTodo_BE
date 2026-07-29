package com.daytodo.domain.user.storage;

import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        assertError(
                new MockMultipartFile("profileImage", "image.jpg", "image/jpeg", image("png")),
                UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT
        );
    }

    @Test
    void rejectsJpegHeaderWhoseBodyCannotBeDecoded() {
        assertError(
                new MockMultipartFile(
                        "profileImage",
                        "broken.jpg",
                        "image/jpeg",
                        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
                ),
                UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT
        );
    }

    @Test
    void validJpegReportsMissingS3ConfigurationOnlyAtUploadTime() {
        assertError(
                new MockMultipartFile("profileImage", "image.jpg", "image/jpeg", image("jpg")),
                UserErrorCode.PROFILE_IMAGE_STORAGE_NOT_CONFIGURED
        );
    }

    @Test
    void validPngReportsMissingS3ConfigurationOnlyAtUploadTime() {
        assertError(
                new MockMultipartFile("profileImage", "image.png", "image/png", image("png")),
                UserErrorCode.PROFILE_IMAGE_STORAGE_NOT_CONFIGURED
        );
    }

    private void assertError(MockMultipartFile file, UserErrorCode errorCode) {
        assertThatThrownBy(() -> storage.upload(1L, file))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private byte[] image(String format) {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, format, output)) {
                throw new IllegalStateException("No ImageIO writer for " + format);
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
