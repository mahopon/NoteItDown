package com.tcyao.nid.note.service;

import com.tcyao.nid.note.dto.CreateNoteRequest;
import com.tcyao.nid.note.dto.CreateNoteResponse;
import com.tcyao.nid.note.dto.GetNoteResponse;
import com.tcyao.nid.note.dto.UpdateNoteRequest;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository repository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createNote_shouldSaveAndReturnResponse() {
        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Text");

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("Test Title");
        savedNote.setText("Test Text");

        when(repository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(1L);
            return savedNote;
        });

        CreateNoteResponse response = noteService.createNote(request);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        Note captured = captor.getValue();

        assertEquals("Test Title", captured.getTitle());
        assertEquals("Test Text", captured.getText());

        assertEquals(1L, response.id());
        assertEquals("Test Title", response.title());
        assertEquals("Test Text", response.text());
    }

    @Test
    void getNote_whenExists_shouldReturnResponse() {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("Title");
        note.setText("Text");

        when(repository.findById(1L)).thenReturn(Optional.of(note));

        GetNoteResponse response = noteService.getNote(1L);

        assertEquals(1L, response.id());
        assertEquals("Title", response.title());
        assertEquals("Text", response.text());
    }

    @Test
    void getNote_whenNotFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> noteService.getNote(99L));
    }

    @Test
    void getAllNotes_whenNotesExist_shouldReturnList() {
        Note note1 = new Note();
        note1.setId(1L);
        note1.setTitle("A");
        note1.setText("a");

        Note note2 = new Note();
        note2.setId(2L);
        note2.setTitle("B");
        note2.setText("b");

        when(repository.findAll()).thenReturn(List.of(note1, note2));

        List<GetNoteResponse> responses = noteService.getAllNotes();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals("A", responses.get(0).title());
        assertEquals("a", responses.get(0).text());
        assertEquals(2L, responses.get(1).id());
        assertEquals("B", responses.get(1).title());
        assertEquals("b", responses.get(1).text());
    }

    @Test
    void getAllNotes_whenNoNotes_shouldReturnEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<GetNoteResponse> responses = noteService.getAllNotes();

        assertTrue(responses.isEmpty());
    }

    @Test
    void updateNote_whenExists_shouldUpdateFields() {
        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setTitle("Old Title");
        existingNote.setText("Old Text");

        UpdateNoteRequest request = new UpdateNoteRequest("New Title", "New Text");

        when(repository.findById(1L)).thenReturn(Optional.of(existingNote));

        noteService.updateNote(1L, request);

        assertEquals("New Title", existingNote.getTitle());
        assertEquals("New Text", existingNote.getText());
        verify(repository).findById(1L);
    }

@Test
    void updateNote_whenNotFound_shouldThrow() {
        UpdateNoteRequest request = new UpdateNoteRequest("Title", "Text");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> noteService.updateNote(99L, request));
    }

    @Test
    void deleteNote_whenExists_shouldDelete() {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("Title");
        note.setText("Text");

        when(repository.findById(1L)).thenReturn(Optional.of(note));

        noteService.deleteNote(1L);

        verify(repository).delete(note);
    }

    @Test
    void deleteNote_whenNotFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> noteService.deleteNote(99L));
    }
}