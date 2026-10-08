package com.tcyao.nid.note.entity;

import com.tcyao.nid.identity.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "notes")
@Getter
public class Note {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private Notebook notebook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by", nullable = false)
    private User lastModifiedBy;

    @ElementCollection
    @CollectionTable(name = "note_attachments", joinColumns = @JoinColumn(name = "note_id"))
    private Set<Attachment> attachments = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant modifiedAt;

    protected Note() {
    }

    Note(String title, String text, User creator, Notebook notebook) {
        this.title = title;
        this.text = text;
        this.createdBy = creator;
        this.lastModifiedBy = creator;
        this.notebook = notebook;
    }

    void update(String title, String text, User editor) {
        if (!Objects.equals(this.title, title)) {
            this.title = title;
        }
        if (!Objects.equals(this.text, text)) {
            this.text = text;
        }
        this.lastModifiedBy = editor;
    }

    /**
     * Replaces the whole attachment set. Used by the reconcile-on-save flow: what is not in
     * {@code newAttachments} is dropped.
     */
    void replaceAttachments(Collection<Attachment> newAttachments) {
        this.attachments.clear();
        this.attachments.addAll(newAttachments);
    }

    public Set<Attachment> getAttachments() {
        return Collections.unmodifiableSet(attachments);
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = Instant.now();
    }
}
