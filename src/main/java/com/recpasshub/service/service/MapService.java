package com.recpasshub.service.service;

import com.recpasshub.service.dto.MapsResponse;
import com.recpasshub.service.entity.Maps;
import com.recpasshub.service.repository.MapsRepository;
import com.recpasshub.service.util.GpxParserUtil;
import com.recpasshub.service.util.GpxParserUtil.GpxMetadata;
import java.io.IOException;
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
public class MapService {

    private final MapsRepository mapMetadataRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Transactional
    public MapsResponse uploadMap(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!"gpx".equalsIgnoreCase(fileExtension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only GPX files are allowed");
        }

        String mapGuid = UUID.randomUUID().toString();
        String s3Key = "maps/" + mapGuid + ".gpx";

        // Parse GPX
        GpxMetadata gpxMetadata;
        try {
            gpxMetadata = GpxParserUtil.parseGpxMetadata(file.getInputStream());
        } catch (IOException e) {
            log.error("Failed to read file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file");
        }

        // Upload to S3
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("application/gpx+xml")
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }

        // Save to DB
        Maps mapMetadata = Maps.builder()
            .mapGuid(mapGuid)
            .s3FileLocation("s3://" + bucketName + "/" + s3Key)
            .distance(gpxMetadata.distance())
            .elevation(gpxMetadata.elevation())
            .location(gpxMetadata.location())
            .build();

        Maps savedMap = mapMetadataRepository.save(mapMetadata);

        return mapToResponse(savedMap);
    }

    @Transactional(readOnly = true)
    public MapsResponse getMapMetadata(String mapGuid) {
        return mapMetadataRepository
            .findByMapGuid(mapGuid)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Map not found"));
    }

    private MapsResponse mapToResponse(Maps map) {
        return new MapsResponse(
            map.getMapGuid(),
            map.getS3FileLocation(),
            map.getDistance(),
            map.getElevation(),
            map.getLocation(),
            map.getCreatedAt(),
            map.getUpdatedAt()
        );
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
}
