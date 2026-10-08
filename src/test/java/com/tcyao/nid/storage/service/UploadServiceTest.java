package com.tcyao.nid.storage.service;

import com.tcyao.nid.storage.dto.CreateUploadRequest;
import com.tcyao.nid.storage.dto.CreateUploadResponse;
import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.exception.UnsupportedArtifactContentTypeException;
import com.tcyao.nid.storage.repository.UploadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    private static final String BUCKET = "nid-uploaded-images";

    @Mock
    private StorageService storageService;

    @Mock
    private UploadRepository uploadRepository;

    @InjectMocks
    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uploadService, "bucket", BUCKET);
    }

    @Test
    void createUpload_shouldSaveAndReturnResponse() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        CreateUploadRequest request = new CreateUploadRequest("photo.png", "image/png");

        when(uploadRepository.save(any(Upload.class))).thenAnswer(invocation -> {
            Upload upload = invocation.getArgument(0);
            upload.setId(uploadId);
            return upload;
        });
        when(storageService.createPutUrl(eq(BUCKET), eq(uploadId), eq("image/png"), any(Duration.class)))
                .thenReturn("https://upload.example/" + uploadId);

        CreateUploadResponse response = uploadService.createUpload(request, userId);

        ArgumentCaptor<Upload> captor = ArgumentCaptor.forClass(Upload.class);
        verify(uploadRepository).save(captor.capture());
        Upload captured = captor.getValue();
        assertEquals("photo.png", captured.getFileName());
        assertEquals("image/png", captured.getFileType());
        assertEquals(userId, captured.getUserId());
        assertNotNull(captured.getUploadedAt());
        assertNull(captured.getReapMarkedAt());

        assertEquals(uploadId, response.id());
        assertEquals("https://upload.example/" + uploadId, response.uploadUrl());
    }

    @Test
    void createUpload_whenUnsupportedContentType_shouldThrow() {
        UUID userId = UUID.randomUUID();
        CreateUploadRequest request = new CreateUploadRequest("script.exe", "application/x-msdownload");

        assertThrows(UnsupportedArtifactContentTypeException.class, () -> uploadService.createUpload(request, userId));
        verify(uploadRepository, never()).save(any(Upload.class));
    }

    @Test
    void presignDownload_shouldReturnUrl() {
        UUID uploadId = UUID.randomUUID();
        when(storageService.createGetUrl(eq(BUCKET), eq(uploadId), any(Duration.class)))
                .thenReturn("https://download.example/" + uploadId);

        assertEquals("https://download.example/" + uploadId, uploadService.presignDownload(uploadId));
    }

    @Test
    void delete_whenExists_shouldDeleteBlobAndRow() {
        UUID uploadId = UUID.randomUUID();
        Upload upload = new Upload("photo.png", "image/png", UUID.randomUUID());
        upload.setId(uploadId);

        when(uploadRepository.findById(uploadId)).thenReturn(Optional.of(upload));

        uploadService.delete(uploadId);

        verify(storageService).delete(BUCKET, uploadId);
        verify(uploadRepository).delete(upload);
    }

    @Test
    void delete_whenMissing_shouldDoNothing() {
        UUID uploadId = UUID.randomUUID();

        when(uploadRepository.findById(uploadId)).thenReturn(Optional.empty());

        uploadService.delete(uploadId);

        verifyNoInteractions(storageService);
        verify(uploadRepository, never()).delete(any(Upload.class));
    }
}
