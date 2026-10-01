package com.tcyao.nid.note.service;

import com.tcyao.nid.note.exception.ArtifactNotFoundException;
import com.tcyao.nid.note.exception.UnsupportedArtifactContentTypeException;
import com.tcyao.nid.storage.dto.CreateArtifactRequest;
import com.tcyao.nid.storage.dto.CreateArtifactResponse;
import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.repository.UploadRepository;
import com.tcyao.nid.storage.service.StorageService;
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
class ArtifactServiceTest {

    private static final String BUCKET = "nid-uploaded-images";

    @Mock
    private StorageService storageService;

    @Mock
    private UploadRepository uploadRepository;

    @InjectMocks
    private ArtifactService artifactService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(artifactService, "bucket", BUCKET);
    }

    @Test
    void createArtifact_shouldSaveAndReturnResponse() {
        UUID userId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();
        CreateArtifactRequest request =
                new CreateArtifactRequest("photo.png", "image/png");

        when(uploadRepository.save(any(Upload.class))).thenAnswer(invocation -> {
            Upload upload = invocation.getArgument(0);
            upload.setId(artifactId);
            return upload;
        });
        when(storageService.createPutUrl(eq(BUCKET), eq(artifactId), eq("image/png"), any(Duration.class)))
                .thenReturn("https://upload.example/" + artifactId);

        CreateArtifactResponse response = artifactService.createArtifact(request, userId);

        ArgumentCaptor<Upload> captor = ArgumentCaptor.forClass(Upload.class);
        verify(uploadRepository).save(captor.capture());
        Upload captured = captor.getValue();
        assertEquals("photo.png", captured.getFileName());
        assertEquals("image/png", captured.getFileType());
        assertEquals(userId, captured.getUserId());
        assertNotNull(captured.getUploadedAt());

        assertEquals(artifactId, response.id());
        assertEquals("https://upload.example/" + artifactId, response.uploadUrl());
        assertEquals("/artifact/" + artifactId, response.url());
    }

    @Test
    void createArtifact_whenUnsupportedContentType_shouldThrow() {
        UUID userId = UUID.randomUUID();
        CreateArtifactRequest request =
                new CreateArtifactRequest("script.exe", "application/x-msdownload");

        assertThrows(UnsupportedArtifactContentTypeException.class,
                () -> artifactService.createArtifact(request, userId));
        verify(uploadRepository, never()).save(any(Upload.class));
    }

    @Test
    void getArtifactDownloadUrl_whenOwner_shouldReturnUrl() {
        UUID userId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();

        Upload upload = new Upload();
        upload.setId(artifactId);
        upload.setUserId(userId);

        when(uploadRepository.findById(artifactId)).thenReturn(Optional.of(upload));
        when(storageService.createGetUrl(eq(BUCKET), eq(artifactId), any(Duration.class)))
                .thenReturn("https://download.example/" + artifactId);

        String url = artifactService.getArtifactDownloadUrl(artifactId, userId);

        assertEquals("https://download.example/" + artifactId, url);
    }

    @Test
    void getArtifactDownloadUrl_whenNotOwner_shouldThrow() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();

        Upload upload = new Upload();
        upload.setId(artifactId);
        upload.setUserId(ownerId);

        when(uploadRepository.findById(artifactId)).thenReturn(Optional.of(upload));

        assertThrows(ArtifactNotFoundException.class,
                () -> artifactService.getArtifactDownloadUrl(artifactId, otherUserId));
        verify(storageService, never()).createGetUrl(any(), any(), any());
    }

    @Test
    void getArtifactDownloadUrl_whenMissing_shouldThrow() {
        UUID userId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();

        when(uploadRepository.findById(artifactId)).thenReturn(Optional.empty());

        assertThrows(ArtifactNotFoundException.class,
                () -> artifactService.getArtifactDownloadUrl(artifactId, userId));
    }
}
