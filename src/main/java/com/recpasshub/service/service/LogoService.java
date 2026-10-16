package com.recpasshub.service.service;

import com.recpasshub.service.dto.LogoResponse;
import com.recpasshub.service.entity.Logo;
import com.recpasshub.service.repository.LogoRepository;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoService {

    /** Allowed file extensions mapped to the content type stored on the S3 object. */
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
        "png",
        "image/png",
        "jpg",
        "image/jpeg",
        "jpeg",
        "image/jpeg",
        "webp",
        "image/webp",
        "svg",
        "image/svg+xml"
    );

    private final LogoRepository logoRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.logo-bucket-name}")
    private String bucketName;

    @Transactional
    public LogoResponse uploadLogo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String contentType = ALLOWED_TYPES.get(fileExtension);
        if (contentType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PNG, JPG, WEBP or SVG files are allowed for logos");
        }

        String logoGuid = UUID.randomUUID().toString();
        String s3Key = "logos/" + logoGuid + "." + fileExtension;

        // Upload to S3
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(s3Key).contentType(contentType).build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            log.error("Failed to upload logo to S3", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }

        // Save to DB
        Logo logo = Logo.builder().logoGuid(logoGuid).s3FileLocation("s3://" + bucketName + "/" + s3Key).build();

        return mapToResponse(logoRepository.save(logo));
    }

    @Transactional(readOnly = true)
    public LogoResponse getLogoMetadata(String logoGuid) {
        return logoRepository
            .findByLogoGuid(logoGuid)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logo not found"));
    }

    private LogoResponse mapToResponse(Logo logo) {
        return new LogoResponse(logo.getLogoGuid(), logo.getS3FileLocation(), logo.getCreatedAt(), logo.getUpdatedAt());
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
}
