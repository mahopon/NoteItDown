package com.tcyao.nid.note.service;

import com.tcyao.nid.note.dto.AttachmentInput;
import com.tcyao.nid.note.entity.Attachment;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.exception.AttachmentNotFoundException;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NotebookRepository;
import com.tcyao.nid.storage.dto.CreateUploadRequest;
import com.tcyao.nid.storage.dto.CreateUploadResponse;
import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.repository.UploadRepository;
import com.tcyao.nid.storage.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final NotebookRepository notebookRepository;
    private final UploadRepository uploadRepository;
    private final UploadService uploadService;

    /**
     * The domain attachment to store, plus a presigned URL when it was newly provisioned
     * (null when the client is keeping an existing upload).
     */
    public record Provisioned(Attachment attachment, String uploadUrl) {
    }

    /**
     * Turns one reconcile entry into a domain attachment, provisioning an upload when the
     * entry has no {@code uploadId}. Existing uploads are guarded by the creator check; the
     * note is the access boundary for reads.
     */
    @Transactional
    public Provisioned provision(AttachmentInput input, UUID userId) {
        if (input.uploadId() != null) {
            requireOwnedUpload(input.uploadId(), userId);
            return new Provisioned(new Attachment(input.uploadId(), input.fileName(), input.contentType()), null);
        }

        CreateUploadResponse upload = uploadService.createUpload(
                new CreateUploadRequest(input.fileName(), input.contentType()), userId);
        return new Provisioned(new Attachment(upload.id(), input.fileName(), input.contentType()), upload.uploadUrl());
    }

    @Transactional(readOnly = true)
    public String downloadUrl(Long notebookId, Long noteId, UUID uploadId, UUID userId) {
        Notebook notebook = requireNotebook(notebookId, userId);
        boolean attached = notebook.getNotes().stream()
                .filter(n -> n.getId() != null && n.getId().equals(noteId))
                .flatMap(n -> n.getAttachments().stream())
                .anyMatch(a -> a.getUploadId().equals(uploadId));
        if (!attached) {
            throw new AttachmentNotFoundException(uploadId);
        }
        return uploadService.presignDownload(uploadId);
    }

    private void requireOwnedUpload(UUID uploadId, UUID userId) {
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new AttachmentNotFoundException(uploadId));
        if (!userId.equals(upload.getUserId())) {
            throw new AttachmentNotFoundException(uploadId);
        }
    }

    private Notebook requireNotebook(Long notebookId, UUID userId) {
        return notebookRepository.findByIdAndOwner_Id(notebookId, userId)
                .orElseThrow(() -> new NotebookNotFoundException(notebookId));
    }
}
