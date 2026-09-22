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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository repository;
    private final NotebookRepository notebookRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateNoteResponse createNote(CreateNoteRequest request, UUID userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(userId.toString()));
        Notebook notebook = notebookRepository.findByIdAndOwner_Id(request.notebookId(), userId)
                .orElseThrow(() -> new NotebookNotFoundException(request.notebookId()));

        Note newNote = new Note();
        newNote.setTitle(request.title());
        newNote.setText(request.text());
        newNote.setNotebook(notebook);
        newNote.setCreatedBy(creator);
        newNote.setLastModifiedBy(creator);

        repository.save(newNote);

        return new CreateNoteResponse(
                newNote.getId(),
                newNote.getTitle(),
                newNote.getText(),
                newNote.getNotebook().getId()
        );
    }

    @Transactional(readOnly = true)
    public GetNoteResponse getNote(Long id, UUID userId) {
        Note foundNote = repository.findByIdAndCreatedBy_Id(id, userId).orElseThrow(() -> new NoteNotFoundException(id));
        return new GetNoteResponse(
                foundNote.getId(),
                foundNote.getTitle(),
                foundNote.getText(),
                foundNote.getNotebook() != null ? foundNote.getNotebook().getId() : null
        );
    }

    @Transactional(readOnly = true)
    public List<GetNoteResponse> getAllNotes(UUID userId) {
        return repository.findByCreatedBy_Id(userId).stream()
                .map(note -> new GetNoteResponse(
                        note.getId(),
                        note.getTitle(),
                        note.getText(),
                        note.getNotebook() != null ? note.getNotebook().getId() : null))
                .toList();
    }

    @Transactional
    public void updateNote(Long id, UUID userId, UpdateNoteRequest request) {
        Note note = repository.findByIdAndCreatedBy_Id(id, userId).orElseThrow(() -> new NoteNotFoundException(id));
        if (note.getTitle() == null || !note.getTitle().equals(request.title()))
            note.setTitle(request.title());
        if (note.getText() == null || !note.getText().equals(request.text()))
            note.setText(request.text());

        if (request.notebookId() != null) {
            Notebook notebook = notebookRepository.findByIdAndOwner_Id(request.notebookId(), userId)
                    .orElseThrow(() -> new NotebookNotFoundException(request.notebookId()));
            note.setNotebook(notebook);
        }

        User editor = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(userId.toString()));
        note.setLastModifiedBy(editor);
    }

    @Transactional
    public void deleteNote(Long id, UUID userId) {
        Note note = repository.findByIdAndCreatedBy_Id(id, userId).orElseThrow(() -> new NoteNotFoundException(id));
        repository.delete(note);
    }
}