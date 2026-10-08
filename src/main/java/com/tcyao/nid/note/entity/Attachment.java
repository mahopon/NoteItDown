package com.tcyao.nid.note.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A value object describing a file attached to a {@link Note}. It references the
 * {@code Upload} aggregate by identity; the bytes live in object storage.
 */
@Embeddable
@Getter
@EqualsAndHashCode(of = "uploadId")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment {

    @Column(name = "upload_id", nullable = false)
    private UUID uploadId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    public Attachment(UUID uploadId, String fileName, String contentType) {
        this.uploadId = uploadId;
        this.fileName = fileName;
        this.contentType = contentType;
    }
}
