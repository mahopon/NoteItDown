package com.tcyao.nid.storage.service;

import com.tcyao.nid.storage.dto.UploadFile;
import com.tcyao.nid.storage.exception.StorageException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Validated
@Service
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public void put(String bucket, @NotNull @Valid UploadFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(file.id().toString())
                    .contentType(file.contentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.content(),
                            file.size()
                    )
            );
        } catch (SdkException e) {
            throw new StorageException("Upload failed", e);
        }
    }

    public String createPutUrl(
            String bucket,
            UUID id,
            String contentType,
            Duration expiration
    ) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(id.toString())
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest =
                    PutObjectPresignRequest.builder()
                            .signatureDuration(expiration)
                            .putObjectRequest(putRequest)
                            .build();

            return s3Presigner
                    .presignPutObject(presignRequest)
                    .url()
                    .toString();

        } catch (SdkException e) {
            throw new StorageException("Failed to create upload URL", e);
        }
    }

    public String createGetUrl(
            String bucket,
            UUID id,
            Duration expiration
    ) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(id.toString())
                    .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(expiration)
                            .getObjectRequest(getRequest)
                            .build();

            return s3Presigner
                    .presignGetObject(presignRequest)
                    .url()
                    .toString();

        } catch (SdkException e) {
            throw new StorageException("Failed to create download URL", e);
        }
    }

    public InputStream get(String bucket, UUID id) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(id.toString())
                    .build();

            return s3Client.getObject(request);

        } catch (SdkException e) {
            throw new StorageException("Download failed", e);
        }
    }

    public void delete(String bucket, UUID id) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(id.toString())
                    .build();

            s3Client.deleteObject(request);

        } catch (SdkException e) {
            throw new StorageException("Delete failed", e);
        }
    }
}