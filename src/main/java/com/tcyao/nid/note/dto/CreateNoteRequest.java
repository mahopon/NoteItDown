package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateNoteRequest(
    @NotBlank String title,
    String text,
    Long notebookId,
    List<Long> tags
) {}