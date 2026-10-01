package com.tcyao.nid.note.exception;

public class UnsupportedArtifactContentTypeException extends RuntimeException {
    public UnsupportedArtifactContentTypeException(String contentType) {
        super("Unsupported content type: " + contentType);
    }
}
