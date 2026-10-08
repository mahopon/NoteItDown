package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * One attachment in a create/update note request (reconcile list).
 *
 * <ul>
 *   <li>{@code uploadId == null} → a new file; the backend provisions an upload and returns
 *       a presigned URL.</li>
 *   <li>{@code uploadId != null} → an existing attachment to keep.</li>
 * </ul>
 */
public record AttachmentInput(
        UUID uploadId,
        @NotBlank String fileName,
        @NotBlank String contentType
) {
}
