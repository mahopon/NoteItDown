package com.tcyao.nid.note.exception;

import java.util.UUID;

public class AttachmentNotFoundException extends RuntimeException {
    public AttachmentNotFoundException(UUID uploadId) {
        super("Attachment not found with id: " + uploadId);
    }
}
