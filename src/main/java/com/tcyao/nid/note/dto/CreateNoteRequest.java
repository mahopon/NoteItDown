package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNoteRequest(
    @NotBlank String title,
    String text
) {}
