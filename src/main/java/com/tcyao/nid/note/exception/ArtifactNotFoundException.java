package com.tcyao.nid.note.exception;

import java.util.UUID;

public class ArtifactNotFoundException extends RuntimeException {
    public ArtifactNotFoundException(UUID id) {
        super("Artifact not found with id: " + id);
    }
}
