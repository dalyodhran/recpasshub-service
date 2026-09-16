package com.recpasshub.service.service;

import com.recpasshub.service.dto.WaiverResponse;
import com.recpasshub.service.entity.Waiver;
import com.recpasshub.service.repository.WaiverRepository;
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
public class WaiverService {

    private final WaiverRepository waiverRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.waiver-bucket-name}")
    private String bucketName;

    @Transactional
    public WaiverResponse uploadWaiver(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!"pdf".equalsIgnoreCase(fileExtension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed for waivers");
        }

        String waiverGuid = UUID.randomUUID().toString();
        String s3Key = "waivers/" + waiverGuid + ".pdf";

        // Upload to S3
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("application/pdf")
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            log.error("Failed to upload waiver to S3", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }

        // Save to DB
        Waiver waiver = Waiver.builder().waiverGuid(waiverGuid).s3FileLocation("s3://" + bucketName + "/" + s3Key).build();

        Waiver savedWaiver = waiverRepository.save(waiver);

        return mapToResponse(savedWaiver);
    }

    @Transactional(readOnly = true)
    public WaiverResponse getWaiverMetadata(String waiverGuid) {
        return waiverRepository
            .findByWaiverGuid(waiverGuid)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waiver not found"));
    }

    private WaiverResponse mapToResponse(Waiver waiver) {
        return new WaiverResponse(waiver.getWaiverGuid(), waiver.getS3FileLocation(), waiver.getCreatedAt(), waiver.getUpdatedAt());
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
}
