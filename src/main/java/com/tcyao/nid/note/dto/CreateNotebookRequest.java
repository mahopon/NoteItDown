package com.tcyao.nid.note.dto;

import com.tcyao.nid.note.enums.CreatableNotebookKind;
import com.tcyao.nid.note.enums.NotebookKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotebookRequest(
        @NotBlank String title,
        @NotNull NotebookKind kind
) {}
