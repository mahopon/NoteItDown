package com.tcyao.nid.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.InputStream;
import java.util.UUID;

public record UploadFile(
        @NotNull InputStream content,
        @Positive long size,
        @NotBlank String contentType,
        @NotBlank String filename,
        @NotBlank UUID id
) {}