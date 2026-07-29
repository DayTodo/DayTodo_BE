package com.daytodo.domain.user.storage;

import com.daytodo.domain.user.exception.code.UserErrorCode;
import com.daytodo.global.apiPayload.exception.ProjectException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Uri;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3ProfileImageStorage implements ProfileImageStorage {

    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Value("${aws.s3.bucket:}")
    private String bucket;

    @Value("${aws.s3.region:}")
    private String region;

    private volatile S3Client s3Client;

    @Override
    public String upload(Long userId, MultipartFile image) {
        ImageType imageType = validate(image);
        S3Client client = client();
        String key = "profile-images/" + userId + "/" + UUID.randomUUID() + imageType.extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(imageType.contentType)
                .contentLength(image.getSize())
                .build();

        try (InputStream inputStream = image.getInputStream()) {
            client.putObject(request, RequestBody.fromInputStream(inputStream, image.getSize()));
            return client.utilities()
                    .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
                    .toString();
        } catch (IOException | SdkException exception) {
            throw new ProjectException(UserErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteByUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return;
        }

        S3Client client = client();
        try {
            S3Uri parsed = client.utilities().parseUri(URI.create(imageUrl));
            if (parsed.bucket().filter(bucket::equals).isEmpty() || parsed.key().isEmpty()) {
                return;
            }
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(parsed.key().orElseThrow())
                    .build());
        } catch (IllegalArgumentException | SdkException exception) {
            throw new ProjectException(UserErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        }
    }

    private ImageType validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ProjectException(UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT);
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new ProjectException(UserErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }

        String contentType = image.getContentType() == null
                ? ""
                : image.getContentType().toLowerCase(Locale.ROOT);
        try (InputStream inputStream = image.getInputStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            byte[] signature = java.util.Arrays.copyOf(
                    imageBytes,
                    Math.min(imageBytes.length, PNG_SIGNATURE.length)
            );
            BufferedImage decodedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (decodedImage == null || decodedImage.getWidth() <= 0 || decodedImage.getHeight() <= 0) {
                throw new ProjectException(UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT);
            }
            if (isJpeg(signature) && "image/jpeg".equals(contentType)) {
                return ImageType.JPEG;
            }
            if (isPng(signature) && "image/png".equals(contentType)) {
                return ImageType.PNG;
            }
        } catch (IOException exception) {
            throw new ProjectException(UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT);
        }
        throw new ProjectException(UserErrorCode.INVALID_PROFILE_IMAGE_FORMAT);
    }

    private boolean isJpeg(byte[] signature) {
        return signature.length >= 3
                && signature[0] == (byte) 0xFF
                && signature[1] == (byte) 0xD8
                && signature[2] == (byte) 0xFF;
    }

    private boolean isPng(byte[] signature) {
        if (signature.length < PNG_SIGNATURE.length) {
            return false;
        }
        for (int index = 0; index < PNG_SIGNATURE.length; index++) {
            if (signature[index] != PNG_SIGNATURE[index]) {
                return false;
            }
        }
        return true;
    }

    private S3Client client() {
        if (!StringUtils.hasText(bucket) || !StringUtils.hasText(region)) {
            throw new ProjectException(UserErrorCode.PROFILE_IMAGE_STORAGE_NOT_CONFIGURED);
        }
        S3Client current = s3Client;
        if (current == null) {
            synchronized (this) {
                current = s3Client;
                if (current == null) {
                    try {
                        current = S3Client.builder()
                                .region(Region.of(region))
                                .credentialsProvider(DefaultCredentialsProvider.create())
                                .build();
                        s3Client = current;
                    } catch (RuntimeException exception) {
                        throw new ProjectException(UserErrorCode.PROFILE_IMAGE_STORAGE_NOT_CONFIGURED);
                    }
                }
            }
        }
        return current;
    }

    @PreDestroy
    void close() {
        S3Client current = s3Client;
        if (current != null) {
            current.close();
        }
    }

    private enum ImageType {
        JPEG(".jpg", "image/jpeg"),
        PNG(".png", "image/png");

        private final String extension;
        private final String contentType;

        ImageType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }
    }
}
