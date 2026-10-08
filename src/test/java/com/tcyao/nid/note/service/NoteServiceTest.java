package com.tcyao.nid.note.service;

import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.identity.repository.UserRepository;
import com.tcyao.nid.note.dto.*;
import com.tcyao.nid.note.entity.Attachment;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.enums.NotebookKind;
import com.tcyao.nid.note.exception.NoteNotFoundException;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NotebookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NotebookRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private NoteService noteService;

    private User user(UUID id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Notebook notebook() {
        Notebook notebook = new Notebook("Notebook", NotebookKind.PERSONAL, user(UUID.randomUUID()));
        notebook.setId(5L);
        notebook.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        notebook.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return notebook;
    }

    @Test
    void createNote_shouldProvisionAttachmentsAndAddNote() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        Notebook notebook = notebook();
        AttachmentInput input = new AttachmentInput(null, "a.png", "image/png");
        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Text", List.of(input));

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(attachmentService.provision(input, userId))
                .thenReturn(new AttachmentService.Provisioned(new Attachment(uploadId, "a.png", "image/png"), "https://upload/" + uploadId));
        when(repository.saveAndFlush(any(Notebook.class))).thenAnswer(invocation -> {
            Notebook nb = invocation.getArgument(0);
            nb.getNotes().forEach(n -> n.setId(1L));
            return nb;
        });

        CreateNoteResponse response = noteService.createNote(5L, request, userId);

        assertEquals(1, notebook.getNotes().size());
        assertEquals(1, notebook.getNotes().get(0).getAttachments().size());
        assertEquals(1L, response.id());
        assertEquals(1, response.attachments().size());
        assertEquals(uploadId, response.attachments().get(0).uploadId());
        assertEquals("https://upload/" + uploadId, response.attachments().get(0).uploadUrl());
    }

    @Test
    void createNote_withNullAttachments_shouldStillWork() {
        UUID userId = UUID.randomUUID();
        Notebook notebook = notebook();
        CreateNoteRequest request = new CreateNoteRequest("Title", "Text", null);

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(repository.saveAndFlush(any(Notebook.class))).thenAnswer(invocation -> {
            Notebook nb = invocation.getArgument(0);
            nb.getNotes().forEach(n -> n.setId(1L));
            return nb;
        });

        CreateNoteResponse response = noteService.createNote(5L, request, userId);

        assertTrue(response.attachments().isEmpty());
        assertTrue(notebook.getNotes().get(0).getAttachments().isEmpty());
        verifyNoInteractions(attachmentService);
    }

    @Test
    void createNote_whenNotebookNotOwned_shouldThrow() {
        UUID userId = UUID.randomUUID();
        CreateNoteRequest request = new CreateNoteRequest("Title", "Text", List.of());

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.empty());

        assertThrows(NotebookNotFoundException.class, () -> noteService.createNote(5L, request, userId));
        verify(repository, never()).saveAndFlush(any(Notebook.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(attachmentService);
    }

    @Test
    void getNotes_shouldReturnNotes() {
        UUID userId = UUID.randomUUID();
        Notebook notebook = notebook();
        Note note = notebook.addNote("Title", "Text", user(userId));
        note.setId(7L);

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));

        List<GetNoteResponse> responses = noteService.getNotes(5L, userId);

        assertEquals(1, responses.size());
        assertEquals(7L, responses.get(0).id());
        assertEquals(5L, responses.get(0).notebookId());
        assertTrue(responses.get(0).attachments().isEmpty());
    }

    @Test
    void getNote_whenExists_returnsAttachmentsWithoutUrl() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        Notebook notebook = notebook();
        Note note = notebook.addNote("Title", "Text", user(userId));
        note.setId(7L);
        notebook.replaceNoteAttachments(7L, List.of(new Attachment(uploadId, "a.png", "image/png")));

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));

        GetNoteResponse response = noteService.getNote(5L, 7L, userId);

        assertEquals(1, response.attachments().size());
        assertEquals(uploadId, response.attachments().get(0).uploadId());
        assertNull(response.attachments().get(0).uploadUrl());
    }

    @Test
    void updateNote_shouldReconcileAttachments() {
        UUID userId = UUID.randomUUID();
        UUID keepId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        Notebook notebook = notebook();
        Note note = notebook.addNote("Old", "Old", user(userId));
        note.setId(7L);
        notebook.replaceNoteAttachments(7L, List.of(new Attachment(keepId, "keep.png", "image/png")));

        AttachmentInput keep = new AttachmentInput(keepId, "keep.png", "image/png");
        AttachmentInput fresh = new AttachmentInput(null, "new.jpg", "image/jpeg");
        UpdateNoteRequest request = new UpdateNoteRequest("New", "New", List.of(keep, fresh));

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(attachmentService.provision(keep, userId))
                .thenReturn(new AttachmentService.Provisioned(new Attachment(keepId, "keep.png", "image/png"), null));
        when(attachmentService.provision(fresh, userId))
                .thenReturn(new AttachmentService.Provisioned(new Attachment(newId, "new.jpg", "image/jpeg"), "https://upload/" + newId));

        GetNoteResponse response = noteService.updateNote(5L, 7L, request, userId);

        assertEquals("New", note.getTitle());
        assertEquals(2, note.getAttachments().size());
        assertEquals(2, response.attachments().size());
        assertNull(response.attachments().get(0).uploadUrl());
        assertEquals("https://upload/" + newId, response.attachments().get(1).uploadUrl());
    }

    @Test
    void updateNote_shouldDropAttachmentsNotInList() {
        UUID userId = UUID.randomUUID();
        UUID removedId = UUID.randomUUID();
        Notebook notebook = notebook();
        Note note = notebook.addNote("Old", "Old", user(userId));
        note.setId(7L);
        notebook.replaceNoteAttachments(7L, List.of(new Attachment(removedId, "gone.png", "image/png")));

        UpdateNoteRequest request = new UpdateNoteRequest("New", "New", List.of());

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));

        noteService.updateNote(5L, 7L, request, userId);

        assertTrue(note.getAttachments().isEmpty());
    }

    @Test
    void updateNote_whenNoteNotInNotebook_shouldThrow() {
        UUID userId = UUID.randomUUID();
        Notebook notebook = notebook();
        UpdateNoteRequest request = new UpdateNoteRequest("Title", "Text", List.of());

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));

        assertThrows(NoteNotFoundException.class, () -> noteService.updateNote(5L, 99L, request, userId));
    }

    @Test
    void deleteNote_shouldRemoveNote() {
        UUID userId = UUID.randomUUID();
        Notebook notebook = notebook();
        Note note = notebook.addNote("Title", "Text", user(userId));
        note.setId(7L);

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));

        noteService.deleteNote(5L, 7L, userId);

        assertTrue(notebook.getNotes().isEmpty());
    }

    @Test
    void deleteNote_whenNoteNotInNotebook_shouldThrow() {
        UUID userId = UUID.randomUUID();
        Notebook notebook = notebook();

        when(repository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));

        assertThrows(NoteNotFoundException.class, () -> noteService.deleteNote(5L, 99L, userId));
    }
}
