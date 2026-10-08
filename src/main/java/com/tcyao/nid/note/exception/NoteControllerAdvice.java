package com.tcyao.nid.note.exception;

import com.tcyao.nid.note.controller.NoteController;
import com.tcyao.nid.storage.exception.UnsupportedArtifactContentTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice(assignableTypes = NoteController.class)
public class NoteControllerAdvice {

    @ExceptionHandler(NoteNotFoundException.class)
    public ProblemDetail handleNoteNotFound(NoteNotFoundException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setTitle("Note Not Found");
        return detail;
    }

    @ExceptionHandler(NotebookNotFoundException.class)
    public ProblemDetail handleNotebookNotFound(NotebookNotFoundException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setTitle("Notebook Not Found");
        return detail;
    }

    @ExceptionHandler(AttachmentNotFoundException.class)
    public ProblemDetail handleAttachmentNotFound(AttachmentNotFoundException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setTitle("Attachment Not Found");
        return detail;
    }

    @ExceptionHandler(UnsupportedArtifactContentTypeException.class)
    public ProblemDetail handleUnsupportedContentType(UnsupportedArtifactContentTypeException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage());
        detail.setTitle("Unsupported Media Type");
        return detail;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElement(NoSuchElementException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setTitle("Resource Not Found");
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setTitle("Validation Error");
        detail.setProperty("errors", e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList());
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        detail.setTitle("Internal Server Error");
        return detail;
    }
}