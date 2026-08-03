package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateNotebookRequest(
        @NotBlank String title
) {}
