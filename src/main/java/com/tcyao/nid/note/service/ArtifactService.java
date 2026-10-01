package com.tcyao.nid.note.service;

import com.tcyao.nid.note.exception.ArtifactNotFoundException;
import com.tcyao.nid.note.exception.UnsupportedArtifactContentTypeException;
import com.tcyao.nid.storage.dto.CreateArtifactRequest;
import com.tcyao.nid.storage.dto.CreateArtifactResponse;
import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.repository.UploadRepository;
import com.tcyao.nid.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtifactService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "application/pdf");
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(10);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(10);

    private final StorageService storageService;
    private final UploadRepository uploadRepository;

    @Value("${UPLOAD_IMAGE_BUCKET}")
    private String bucket;

    @Transactional
    public CreateArtifactResponse createArtifact(CreateArtifactRequest request, UUID userId) {
        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw new UnsupportedArtifactContentTypeException(request.contentType());
        }

        Upload upload = new Upload();
        upload.setFileName(request.fileName());
        upload.setFileType(request.contentType());
        upload.setUserId(userId);
        upload.setUploadedAt(Instant.now());
        Upload saved = uploadRepository.save(upload);

        UUID id = saved.getId();
        String uploadUrl = storageService.createPutUrl(
                bucket,
                id,
                request.contentType(),
                UPLOAD_URL_TTL
        );

        return new CreateArtifactResponse(id, uploadUrl, "/artifact/" + id);
    }

    @Transactional(readOnly = true)
    public String getArtifactDownloadUrl(UUID id, UUID userId) {
        // Only the owner can read the artifact; treat others as not found so we
        // don't leak the existence of someone else's upload.
        uploadRepository.findById(id)
                .filter(upload -> userId.equals(upload.getUserId()))
                .orElseThrow(() -> new ArtifactNotFoundException(id));

        return storageService.createGetUrl(bucket, id, DOWNLOAD_URL_TTL);
    }
}
