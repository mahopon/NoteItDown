package com.tcyao.nid.note.entity;

import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.note.enums.NotebookKind;
import com.tcyao.nid.note.exception.NoteNotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "notebooks")
@Getter
public class Notebook {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotebookKind kind;

    @OneToMany(mappedBy = "notebook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @Setter
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Setter
    @Column(nullable = false)
    private Instant modifiedAt;

    protected Notebook() {
    }

    public Notebook(String title, NotebookKind kind, User owner) {
        this.title = title;
        this.kind = kind;
        this.owner = owner;
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

    public void updateTitle(String title) {
        this.title = title;
        touch();
    }

    /**
     * Creates a note inside this notebook. The notebook is the aggregate root, so this is
     * the only supported way to add a note.
     */
    public Note addNote(String title, String text, User creator) {
        return addNote(title, text, List.of(), creator);
    }

    /**
     * Creates a note carrying the given attachments. Attachments are provisioned (and their
     * uploads created) by the application layer before this call.
     */
    public Note addNote(String title, String text, List<Attachment> attachments, User creator) {
        Note note = new Note(title, text, creator, this);
        note.replaceAttachments(attachments);
        notes.add(note);
        touch();
        return note;
    }

    /**
     * Updates a note that belongs to this notebook.
     */
    public void updateNote(Long noteId, String title, String text, User editor) {
        requireNote(noteId).update(title, text, editor);
        touch();
    }

    /**
     * Replaces a note's attachment set (reconcile-on-save).
     */
    public void replaceNoteAttachments(Long noteId, List<Attachment> attachments) {
        requireNote(noteId).replaceAttachments(attachments);
        touch();
    }

    /**
     * Removes a note from this notebook. The note cannot exist on its own, so it is deleted.
     */
    public void removeNote(Long noteId) {
        notes.remove(requireNote(noteId));
        touch();
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    private Note requireNote(Long noteId) {
        return notes.stream()
                .filter(n -> n.getId() != null && n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new NoteNotFoundException(noteId));
    }

    private void touch() {
        this.modifiedAt = Instant.now();
    }
}
