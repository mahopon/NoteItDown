package com.tcyao.nid.storage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "uploads")
@Getter
public class Upload {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    /**
     * Set by the reaper when the upload is no longer referenced by any note. It is deleted
     * on a later run, giving a grace period in which a re-attach can cancel the deletion.
     */
    @Column(name = "reap_marked_at")
    private Instant reapMarkedAt;

    protected Upload() {
    }

    public Upload(String fileName, String fileType, UUID userId) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.userId = userId;
        this.uploadedAt = Instant.now();
    }
}
