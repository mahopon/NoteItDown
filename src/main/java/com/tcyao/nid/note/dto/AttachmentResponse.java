package com.tcyao.nid.note.dto;

import java.util.UUID;

public record AttachmentResponse(
        UUID uploadId,
        String fileName,
        String contentType,
        /** Presigned PUT URL; present only for newly provisioned attachments. */
        String uploadUrl
) {
}
