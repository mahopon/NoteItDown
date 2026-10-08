package com.tcyao.nid.note.service;

import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.note.dto.AttachmentInput;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.enums.NotebookKind;
import com.tcyao.nid.note.exception.AttachmentNotFoundException;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NotebookRepository;
import com.tcyao.nid.storage.dto.CreateUploadRequest;
import com.tcyao.nid.storage.dto.CreateUploadResponse;
import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.repository.UploadRepository;
import com.tcyao.nid.storage.service.UploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private UploadRepository uploadRepository;

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private AttachmentService attachmentService;

    private Notebook notebookWithNote(UUID ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        Notebook notebook = new Notebook("Notebook", NotebookKind.PERSONAL, owner);
        notebook.setId(5L);
        notebook.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        notebook.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));
        Note note = notebook.addNote("Title", "Text", owner);
        note.setId(7L);
        return notebook;
    }

    private Upload upload(UUID id, UUID ownerId) {
        Upload upload = new Upload("photo.png", "image/png", ownerId);
        upload.setId(id);
        return upload;
    }

    @Test
    void provision_whenNew_createsUploadAndReturnsUrl() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        AttachmentInput input = new AttachmentInput(null, "photo.png", "image/png");

        when(uploadService.createUpload(new CreateUploadRequest("photo.png", "image/png"), userId))
                .thenReturn(new CreateUploadResponse(uploadId, "https://upload.example/" + uploadId));

        AttachmentService.Provisioned provisioned = attachmentService.provision(input, userId);

        assertEquals(uploadId, provisioned.attachment().getUploadId());
        assertEquals("photo.png", provisioned.attachment().getFileName());
        assertEquals("https://upload.example/" + uploadId, provisioned.uploadUrl());
    }

    @Test
    void provision_whenExistingOwned_keepsItWithNoUrl() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        AttachmentInput input = new AttachmentInput(uploadId, "photo.png", "image/png");

        when(uploadRepository.findById(uploadId)).thenReturn(Optional.of(upload(uploadId, userId)));

        AttachmentService.Provisioned provisioned = attachmentService.provision(input, userId);

        assertEquals(uploadId, provisioned.attachment().getUploadId());
        assertNull(provisioned.uploadUrl());
        verifyNoInteractions(uploadService);
    }

    @Test
    void provision_whenExistingNotOwned_throws() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        AttachmentInput input = new AttachmentInput(uploadId, "photo.png", "image/png");

        when(uploadRepository.findById(uploadId)).thenReturn(Optional.of(upload(uploadId, UUID.randomUUID())));

        assertThrows(AttachmentNotFoundException.class, () -> attachmentService.provision(input, userId));
        verifyNoInteractions(uploadService);
    }

    @Test
    void downloadUrl_whenAttached_returnsPresignedUrl() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        Notebook notebook = notebookWithNote(userId);
        notebook.replaceNoteAttachments(7L, java.util.List.of(
                new com.tcyao.nid.note.entity.Attachment(uploadId, "photo.png", "image/png")));

        when(notebookRepository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));
        when(uploadService.presignDownload(uploadId)).thenReturn("https://download.example/" + uploadId);

        assertEquals("https://download.example/" + uploadId, attachmentService.downloadUrl(5L, 7L, uploadId, userId));
    }

    @Test
    void downloadUrl_whenNotAttached_throws() {
        UUID userId = UUID.randomUUID();
        Notebook notebook = notebookWithNote(userId);

        when(notebookRepository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));

        assertThrows(AttachmentNotFoundException.class,
                () -> attachmentService.downloadUrl(5L, 7L, UUID.randomUUID(), userId));
        verifyNoInteractions(uploadService);
    }

    @Test
    void downloadUrl_whenNotebookNotOwned_throws() {
        UUID userId = UUID.randomUUID();

        when(notebookRepository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.empty());

        assertThrows(NotebookNotFoundException.class,
                () -> attachmentService.downloadUrl(5L, 7L, UUID.randomUUID(), userId));
    }
}
