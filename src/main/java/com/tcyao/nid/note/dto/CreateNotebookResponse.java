package com.tcyao.nid.note.dto;

import java.time.Instant;

public record CreateNotebookResponse(
        Long id,
        String title,
        Instant createdAt,
        Instant modifiedAt
) {}
