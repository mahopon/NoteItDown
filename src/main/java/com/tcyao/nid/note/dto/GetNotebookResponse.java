package com.tcyao.nid.note.dto;

import java.time.Instant;
import java.util.List;

public record GetNotebookResponse(
        Long id,
        String title,
        List<GetNoteResponse> notes,
        Instant createdAt,
        Instant modifiedAt
) {}
