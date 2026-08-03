package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateNoteRequest(
        @NotBlank String title,
        String text
) {
}
