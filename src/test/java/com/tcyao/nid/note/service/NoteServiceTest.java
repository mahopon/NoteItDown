package com.tcyao.nid.note.service;

import com.tcyao.nid.note.dto.CreateNoteRequest;
import com.tcyao.nid.note.dto.CreateNoteResponse;
import com.tcyao.nid.note.dto.GetNoteResponse;
import com.tcyao.nid.note.dto.TagResponse;
import com.tcyao.nid.note.dto.UpdateNoteRequest;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.entity.Tag;
import com.tcyao.nid.note.repository.NoteRepository;
import com.tcyao.nid.note.repository.NotebookRepository;
import com.tcyao.nid.note.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import com.tcyao.nid.note.exception.NoteNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository repository;

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createNote_shouldSaveAndReturnResponse() {
        Tag tagA = new Tag("tagA");
        tagA.setId(1L);

        CreateNoteRequest request = new CreateNoteRequest("Test Title", "Test Text", null, List.of(1L));

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("Test Title");
        savedNote.setText("Test Text");

        when(repository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(1L);
            return savedNote;
        });
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tagA));

        CreateNoteResponse response = noteService.createNote(request);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        Note captured = captor.getValue();

        assertEquals("Test Title", captured.getTitle());
        assertEquals("Test Text", captured.getText());
        assertEquals(1, captured.getTags().size());
        assertTrue(captured.getTags().stream().anyMatch(t -> t.getName().equals("tagA")));

        assertEquals(1L, response.id());
        assertEquals("Test Title", response.title());
        assertEquals("Test Text", response.text());
        assertNull(response.notebookId());
        assertEquals(List.of(new TagResponse(1L, "tagA")), response.tags());
    }

    @Test
    void createNote_withNotebook_shouldAssignNotebook() {
        Notebook notebook = new Notebook();
        notebook.setId(5L);

        CreateNoteRequest request = new CreateNoteRequest("Title", "Text", 5L, null);

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("Title");
        savedNote.setText("Text");
        savedNote.setNotebook(notebook);

        when(notebookRepository.findById(5L)).thenReturn(Optional.of(notebook));
        when(repository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(1L);
            return savedNote;
        });

        CreateNoteResponse response = noteService.createNote(request);

        assertEquals(5L, response.notebookId());
        assertTrue(response.tags().isEmpty());
    }

    @Test
    void getNote_whenExists_shouldReturnResponse() {
        Notebook notebook = new Notebook();
        notebook.setId(2L);

        Tag tag = new Tag("urgent");
        tag.setId(1L);

        Note note = new Note();
        note.setId(1L);
        note.setTitle("Title");
        note.setText("Text");
        note.setNotebook(notebook);
        note.getTags().add(tag);

        when(repository.findById(1L)).thenReturn(Optional.of(note));

        GetNoteResponse response = noteService.getNote(1L);

        assertEquals(1L, response.id());
        assertEquals("Title", response.title());
        assertEquals("Text", response.text());
        assertEquals(2L, response.notebookId());
        assertEquals(List.of(new TagResponse(1L, "urgent")), response.tags());
    }

    @Test
    void getNote_whenNotFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.getNote(99L));
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
        assertNull(responses.get(0).notebookId());
        assertTrue(responses.get(0).tags().isEmpty());
        assertEquals(2L, responses.get(1).id());
        assertEquals("B", responses.get(1).title());
        assertEquals("b", responses.get(1).text());
        assertNull(responses.get(1).notebookId());
        assertTrue(responses.get(1).tags().isEmpty());
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

        Tag tagA = new Tag("tagA");
        tagA.setId(1L);
        existingNote.getTags().add(tagA);

        Tag tagC = new Tag("tagC");
        tagC.setId(2L);

        UpdateNoteRequest request = new UpdateNoteRequest("New Title", "New Text", null, List.of(1L, 2L));

        when(repository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tagA));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tagC));

        noteService.updateNote(1L, request);

        assertEquals("New Title", existingNote.getTitle());
        assertEquals("New Text", existingNote.getText());
        assertNull(existingNote.getNotebook());
        assertEquals(2, existingNote.getTags().size());
        assertTrue(existingNote.getTags().stream().anyMatch(t -> t.getId().equals(1L)));
        assertTrue(existingNote.getTags().stream().anyMatch(t -> t.getId().equals(2L)));
        verify(repository).findById(1L);
    }

    @Test
    void updateNote_whenTagsMatch_shouldNotModifyTags() {
        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setTitle("Title");
        existingNote.setText("Text");

        Tag tagA = new Tag("tagA");
        tagA.setId(1L);
        existingNote.getTags().add(tagA);

        UpdateNoteRequest request = new UpdateNoteRequest("Title", "Text", null, List.of(1L));

        when(repository.findById(1L)).thenReturn(Optional.of(existingNote));

        noteService.updateNote(1L, request);

        assertEquals(1, existingNote.getTags().size());
        verify(tagRepository, never()).findById(anyLong());
    }

    @Test
    void updateNote_withNotebook_shouldAssignNotebook() {
        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setTitle("Title");
        existingNote.setText("Text");

        Notebook notebook = new Notebook();
        notebook.setId(10L);

        UpdateNoteRequest request = new UpdateNoteRequest("Title", "Text", 10L, null);

        when(repository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(notebookRepository.findById(10L)).thenReturn(Optional.of(notebook));

        noteService.updateNote(1L, request);

        assertNotNull(existingNote.getNotebook());
        assertEquals(10L, existingNote.getNotebook().getId());
    }

    @Test
    void updateNote_whenNotFound_shouldThrow() {
        UpdateNoteRequest request = new UpdateNoteRequest("Title", "Text", null, null);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.updateNote(99L, request));
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

        assertThrows(NoteNotFoundException.class, () -> noteService.deleteNote(99L));
    }
}