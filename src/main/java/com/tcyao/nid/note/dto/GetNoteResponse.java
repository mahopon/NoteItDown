package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;

public record GetNoteResponse(
        Long id,
        String title,
        String text
) {}
