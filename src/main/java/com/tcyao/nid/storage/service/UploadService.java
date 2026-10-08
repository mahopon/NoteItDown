package com.tcyao.nid.storage.service;

import com.tcyao.nid.storage.dto.CreateUploadRequest;
import com.tcyao.nid.storage.dto.CreateUploadResponse;
import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.exception.UnsupportedArtifactContentTypeException;
import com.tcyao.nid.storage.repository.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Storage-facing application service. Owns the upload intent (metadata + presigned URL)
 * and blob lifecycle. It is intentionally unaware of notes/notebooks.
 */
@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "application/pdf");
    private static final Duration URL_TTL = Duration.ofMinutes(5);

    private final StorageService storageService;
    private final UploadRepository uploadRepository;

    @Value("${UPLOAD_IMAGE_BUCKET}")
    private String bucket;

    @Transactional
    public CreateUploadResponse createUpload(CreateUploadRequest request, UUID userId) {
        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw new UnsupportedArtifactContentTypeException(request.contentType());
        }

        Upload upload = new Upload(request.fileName(), request.contentType(), userId);
        uploadRepository.save(upload);

        String uploadUrl = storageService.createPutUrl(bucket, upload.getId(), request.contentType(), URL_TTL);
        return new CreateUploadResponse(upload.getId(), uploadUrl);
    }

    @Transactional(readOnly = true)
    public String presignDownload(UUID uploadId) {
        return storageService.createGetUrl(bucket, uploadId, URL_TTL);
    }

    @Transactional
    public void delete(UUID uploadId) {
        uploadRepository.findById(uploadId).ifPresent(upload -> {
            storageService.delete(bucket, uploadId);
            uploadRepository.delete(upload);
        });
    }
}
