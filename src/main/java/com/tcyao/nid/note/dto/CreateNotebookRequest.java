package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNotebookRequest(
        @NotBlank String title
) {}
