package com.tcyao.nid.note.service;

import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.identity.repository.UserRepository;
import com.tcyao.nid.note.dto.CreateNoteRequest;
import com.tcyao.nid.note.dto.CreateNoteResponse;
import com.tcyao.nid.note.dto.GetNoteResponse;
import com.tcyao.nid.note.dto.UpdateNoteRequest;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.exception.NoteNotFoundException;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NoteRepository;
import com.tcyao.nid.note.repository.NotebookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository repository;

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createNote_shouldSaveAndReturnResponse() {
        UUID userId = UUID.randomUUID();
        User creator = new User();
        creator.setId(userId);

        Notebook notebook = new Notebook();
        notebook.setId(5L);

        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Text", 5L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
        when(notebookRepository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.of(notebook));
        when(repository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(1L);
            return note;
        });

        CreateNoteResponse response = noteService.createNote(request, userId);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        Note captured = captor.getValue();

        assertEquals("Test Title", captured.getTitle());
        assertEquals("Test Text", captured.getText());
        assertEquals(notebook, captured.getNotebook());
        assertEquals(creator, captured.getCreatedBy());
        assertEquals(creator, captured.getLastModifiedBy());

        assertEquals(1L, response.id());
        assertEquals("Test Title", response.title());
        assertEquals("Test Text", response.text());
        assertEquals(5L, response.notebookId());
    }

    @Test
    void createNote_whenNotebookNotOwned_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User creator = new User();
        creator.setId(userId);

        CreateNoteRequest request = new CreateNoteRequest("Title", "Text", 5L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
        when(notebookRepository.findByIdAndOwner_Id(5L, userId)).thenReturn(Optional.empty());

        assertThrows(NotebookNotFoundException.class, () -> noteService.createNote(request, userId));
        verify(repository, never()).save(any(Note.class));
    }

    @Test
    void createNote_whenUserNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();
        CreateNoteRequest request = new CreateNoteRequest("Title", "Text", 5L);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> noteService.createNote(request, userId));
        verify(repository, never()).save(any(Note.class));
    }

    @Test
    void getNote_whenExists_shouldReturnResponse() {
        UUID userId = UUID.randomUUID();
        Notebook notebook = new Notebook();
        notebook.setId(2L);

        Note note = new Note();
        note.setId(1L);
        note.setTitle("Title");
        note.setText("Text");
        note.setNotebook(notebook);

        when(repository.findByIdAndCreatedBy_Id(1L, userId)).thenReturn(Optional.of(note));

        GetNoteResponse response = noteService.getNote(1L, userId);

        assertEquals(1L, response.id());
        assertEquals("Title", response.title());
        assertEquals("Text", response.text());
        assertEquals(2L, response.notebookId());
    }

    @Test
    void getNote_whenNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();

        when(repository.findByIdAndCreatedBy_Id(99L, userId)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.getNote(99L, userId));
    }

    @Test
    void getAllNotes_whenNotesExist_shouldReturnList() {
        UUID userId = UUID.randomUUID();

        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("A");
        note1.setText("a");

        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("B");
        note2.setText("b");

        when(repository.findByCreatedBy_Id(userId)).thenReturn(List.of(note1, note2));

        List<GetNoteResponse> responses = noteService.getAllNotes(userId);

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals("A", responses.get(0).title());
        assertEquals("a", responses.get(0).text());
        assertNull(responses.get(0).notebookId());
        assertEquals(2L, responses.get(1).id());
        assertEquals("B", responses.get(1).title());
        assertEquals("b", responses.get(1).text());
        assertNull(responses.get(1).notebookId());
    }

    @Test
    void getAllNotes_whenNoNotes_shouldReturnEmptyList() {
        UUID userId = UUID.randomUUID();

        when(repository.findByCreatedBy_Id(userId)).thenReturn(List.of());

        List<GetNoteResponse> responses = noteService.getAllNotes(userId);

        assertTrue(responses.isEmpty());
    }

    @Test
    void updateNote_whenExists_shouldUpdateFields() {
        UUID userId = UUID.randomUUID();
        User editor = new User();
        editor.setId(userId);

        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setTitle("Old Title");
        existingNote.setText("Old Text");

        UpdateNoteRequest request = new UpdateNoteRequest("New Title", "New Text", null);

        when(repository.findByIdAndCreatedBy_Id(1L, userId)).thenReturn(Optional.of(existingNote));
        when(userRepository.findById(userId)).thenReturn(Optional.of(editor));

        noteService.updateNote(1L, userId, request);

        assertEquals("New Title", existingNote.getTitle());
        assertEquals("New Text", existingNote.getText());
        assertNull(existingNote.getNotebook());
        assertEquals(editor, existingNote.getLastModifiedBy());
        verify(repository).findByIdAndCreatedBy_Id(1L, userId);
    }

    @Test
    void updateNote_withNotebook_shouldAssignNotebook() {
        UUID userId = UUID.randomUUID();
        User editor = new User();
        editor.setId(userId);

        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setTitle("Title");
        existingNote.setText("Text");

        Notebook notebook = new Notebook();
        notebook.setId(10L);

        UpdateNoteRequest request = new UpdateNoteRequest("Title", "Text", 10L);

        when(repository.findByIdAndCreatedBy_Id(1L, userId)).thenReturn(Optional.of(existingNote));
        when(notebookRepository.findByIdAndOwner_Id(10L, userId)).thenReturn(Optional.of(notebook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(editor));

        noteService.updateNote(1L, userId, request);

        assertNotNull(existingNote.getNotebook());
        assertEquals(10L, existingNote.getNotebook().getId());
        assertEquals(editor, existingNote.getLastModifiedBy());
    }

    @Test
    void updateNote_whenNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();

        UpdateNoteRequest request = new UpdateNoteRequest("Title", "Text", null);

        when(repository.findByIdAndCreatedBy_Id(99L, userId)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.updateNote(99L, userId, request));
    }

    @Test
    void deleteNote_whenExists_shouldDelete() {
        UUID userId = UUID.randomUUID();

        Note note = new Note();
        note.setId(1L);
        note.setTitle("Title");
        note.setText("Text");

        when(repository.findByIdAndCreatedBy_Id(1L, userId)).thenReturn(Optional.of(note));

        noteService.deleteNote(1L, userId);

        verify(repository).delete(note);
    }

    @Test
    void deleteNote_whenNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();

        when(repository.findByIdAndCreatedBy_Id(99L, userId)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.deleteNote(99L, userId));
    }
}