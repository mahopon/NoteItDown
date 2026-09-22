package com.tcyao.nid.note.enums;

public enum CreatableNotebookKind {
    PERSONAL,
    SHARED;

    public NotebookKind toNotebookKind() {
        return switch (this) {
            case PERSONAL -> NotebookKind.PERSONAL;
            case SHARED -> NotebookKind.SHARED;
        };
    }
}