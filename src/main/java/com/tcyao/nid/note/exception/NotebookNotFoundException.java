package com.tcyao.nid.note.exception;

public class NotebookNotFoundException extends RuntimeException {
    public NotebookNotFoundException(Long id) {
        super("Notebook not found with id: " + id);
    }
}