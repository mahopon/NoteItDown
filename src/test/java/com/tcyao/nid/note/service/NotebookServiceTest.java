package com.tcyao.nid.note.service;

import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.identity.repository.UserRepository;
import com.tcyao.nid.note.dto.*;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.enums.CreatableNotebookKind;
import com.tcyao.nid.note.enums.NotebookKind;
import com.tcyao.nid.note.repository.NotebookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import com.tcyao.nid.note.exception.NotebookNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

    @Mock
    private NotebookRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotebookService notebookService;

    @Test
    void createNotebook_shouldSaveAndReturnResponse() {
        UUID ownerId = UUID.randomUUID();
        CreateNotebookRequest request = new CreateNotebookRequest("My Notebook", CreatableNotebookKind.PERSONAL.toNotebookKind());

        User owner = new User();
        owner.setId(ownerId);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Notebook savedNotebook = new Notebook();
        savedNotebook.setId(1L);
        savedNotebook.setTitle("My Notebook");
        savedNotebook.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        savedNotebook.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(repository.save(any(Notebook.class))).thenAnswer(invocation -> {
            Notebook nb = invocation.getArgument(0);
            nb.setId(1L);
            nb.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            nb.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return savedNotebook;
        });

        CreateNotebookResponse response = notebookService.createNotebook(request, ownerId);

        ArgumentCaptor<Notebook> captor = ArgumentCaptor.forClass(Notebook.class);
        verify(repository).save(captor.capture());
        Notebook captured = captor.getValue();

        assertEquals("My Notebook", captured.getTitle());
        assertEquals(NotebookKind.PERSONAL, captured.getKind());
        assertEquals(owner, captured.getOwner());
        assertEquals(1L, response.id());
        assertEquals("My Notebook", response.title());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.createdAt());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.modifiedAt());
    }

    @Test
    void createNotebook_whenOwnerNotFound_shouldThrow() {
        UUID ownerId = UUID.randomUUID();
        CreateNotebookRequest request = new CreateNotebookRequest("My Notebook", CreatableNotebookKind.SHARED.toNotebookKind());

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> notebookService.createNotebook(request, ownerId));
        verify(repository, never()).save(any(Notebook.class));
    }

    @Test
    void getNotebook_whenExists_shouldReturnResponse() {
        UUID ownerId = UUID.randomUUID();

        Notebook notebook = new Notebook();
        notebook.setId(1L);
        notebook.setTitle("My Notebook");
        notebook.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        notebook.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));

        Note note = new Note();
        note.setId(10L);
        note.setTitle("Note Title");
        note.setText("Note Text");
        notebook.getNotes().add(note);

        when(repository.findByIdAndOwner_Id(1L, ownerId)).thenReturn(Optional.of(notebook));

        GetNotebookResponse response = notebookService.getNotebook(1L, ownerId);

        assertEquals(1L, response.id());
        assertEquals("My Notebook", response.title());
        assertEquals(1, response.notes().size());
        assertEquals(10L, response.notes().get(0).id());
        assertEquals("Note Title", response.notes().get(0).title());
        assertEquals("Note Text", response.notes().get(0).text());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.createdAt());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), response.modifiedAt());
    }

    @Test
    void getNotebook_whenNotFound_shouldThrow() {
        UUID ownerId = UUID.randomUUID();

        when(repository.findByIdAndOwner_Id(99L, ownerId)).thenReturn(Optional.empty());

        assertThrows(NotebookNotFoundException.class, () -> notebookService.getNotebook(99L, ownerId));
    }

    @Test
    void getAllNotebooks_whenNotebooksExist_shouldReturnList() {
        UUID ownerId = UUID.randomUUID();

        Notebook nb1 = new Notebook();
        nb1.setId(1L);
        nb1.setTitle("First");
        nb1.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        nb1.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));

        Notebook nb2 = new Notebook();
        nb2.setId(2L);
        nb2.setTitle("Second");
        nb2.setCreatedAt(Instant.parse("2026-01-02T00:00:00Z"));
        nb2.setModifiedAt(Instant.parse("2026-01-02T00:00:00Z"));

        Note note1 = new Note();
        note1.setId(10L);
        note1.setTitle("A");
        note1.setText("a");
        nb1.getNotes().add(note1);

        when(repository.findByOwner_Id(ownerId)).thenReturn(List.of(nb1, nb2));

        List<GetNotebookResponse> responses = notebookService.getAllNotebooks(ownerId);

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals("First", responses.get(0).title());
        assertEquals(1, responses.get(0).notes().size());
        assertEquals(10L, responses.get(0).notes().get(0).id());
        assertEquals(2L, responses.get(1).id());
        assertEquals("Second", responses.get(1).title());
        assertEquals(0, responses.get(1).notes().size());
    }

    @Test
    void getAllNotebooks_whenNoNotebooks_shouldReturnEmptyList() {
        UUID ownerId = UUID.randomUUID();

        when(repository.findByOwner_Id(ownerId)).thenReturn(List.of());

        List<GetNotebookResponse> responses = notebookService.getAllNotebooks(ownerId);

        assertTrue(responses.isEmpty());
    }

    @Test
    void updateNotebook_whenExists_shouldUpdateTitleAndModifiedAt() {
        UUID ownerId = UUID.randomUUID();

        Notebook existingNotebook = new Notebook();
        existingNotebook.setId(1L);
        existingNotebook.setTitle("Old Title");
        existingNotebook.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        existingNotebook.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));

        UpdateNotebookRequest request = new UpdateNotebookRequest("New Title");

        when(repository.findByIdAndOwner_Id(1L, ownerId)).thenReturn(Optional.of(existingNotebook));

        notebookService.updateNotebook(1L, ownerId, request);

        assertEquals("New Title", existingNotebook.getTitle());
        assertTrue(existingNotebook.getModifiedAt().isAfter(Instant.parse("2026-01-01T00:00:00Z")));
        verify(repository).findByIdAndOwner_Id(1L, ownerId);
    }

    @Test
    void updateNotebook_whenNotFound_shouldThrow() {
        UUID ownerId = UUID.randomUUID();
        UpdateNotebookRequest request = new UpdateNotebookRequest("Title");

        when(repository.findByIdAndOwner_Id(99L, ownerId)).thenReturn(Optional.empty());

        assertThrows(NotebookNotFoundException.class, () -> notebookService.updateNotebook(99L, ownerId, request));
    }

    @Test
    void deleteNotebook_whenExists_shouldDelete() {
        UUID ownerId = UUID.randomUUID();

        Notebook notebook = new Notebook();
        notebook.setId(1L);
        notebook.setTitle("Title");
        notebook.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        notebook.setModifiedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(repository.findByIdAndOwner_Id(1L, ownerId)).thenReturn(Optional.of(notebook));

        notebookService.deleteNotebook(1L, ownerId);

        verify(repository).delete(notebook);
    }

    @Test
    void deleteNotebook_whenNotFound_shouldThrow() {
        UUID ownerId = UUID.randomUUID();

        when(repository.findByIdAndOwner_Id(99L, ownerId)).thenReturn(Optional.empty());

        assertThrows(NotebookNotFoundException.class, () -> notebookService.deleteNotebook(99L, ownerId));
    }
}