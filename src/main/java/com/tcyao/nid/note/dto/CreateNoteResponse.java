package com.tcyao.nid.note.dto;

import java.util.List;

public record CreateNoteResponse(
        Long id,
        String title,
        String text,
        Long notebookId,
        List<TagResponse> tags
) {}