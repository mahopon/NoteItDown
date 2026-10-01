package com.tcyao.nid.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateArtifactRequest(
        @NotBlank String fileName,
        @NotBlank String contentType
) {
}
