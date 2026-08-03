package com.tcyao.nid.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotePresencePayload(
        @NotNull int status // 1 for active, 2 for away
) { }
