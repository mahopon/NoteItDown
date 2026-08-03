package com.tcyao.nid.note.dto;

public record CreateNoteResponse(
        Long id,
        String title,
        String text
) {}
