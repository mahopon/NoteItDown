package com.tcyao.nid.storage.dto;

import java.util.UUID;

public record CreateArtifactResponse(
        UUID id,
        /** Presigned S3 URL to PUT the file to. */
        String uploadUrl,
        /** Relative URL the client embeds to read the artifact back. */
        String url
) {
}
