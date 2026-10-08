package com.tcyao.nid.storage.dto;

import java.util.UUID;

public record CreateUploadResponse(
        UUID id,
        /** Presigned S3 URL to PUT the file to. */
        String uploadUrl
) {
}
