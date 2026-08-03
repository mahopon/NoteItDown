package com.tcyao.nid.note.service;

import com.tcyao.nid.note.dto.CreateNoteRequest;
import com.tcyao.nid.note.dto.CreateNoteResponse;
import com.tcyao.nid.note.dto.GetNoteResponse;
import com.tcyao.nid.note.dto.UpdateNoteRequest;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.entity.Tag;
import com.tcyao.nid.note.exception.NoteNotFoundException;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NoteRepository;
import com.tcyao.nid.note.repository.NotebookRepository;
import com.tcyao.nid.note.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository repository;
    private final NotebookRepository notebookRepository;
    private final TagRepository tagRepository;

    public CreateNoteResponse createNote(CreateNoteRequest request) {
        Note newNote = new Note();
        newNote.setTitle(request.title());
        newNote.setText(request.text());

        if (request.notebookId() != null) {
            Notebook notebook = notebookRepository.findById(request.notebookId()).orElseThrow(() -> new NotebookNotFoundException(request.notebookId()));
            newNote.setNotebook(notebook);
        }

        if (request.tags() != null) {
            request.tags().forEach(tagId -> {
                Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));
                newNote.getTags().add(tag);
            });
        }

        repository.save(newNote);

        return new CreateNoteResponse(
                newNote.getId(),
                newNote.getTitle(),
                newNote.getText(),
                newNote.getNotebook() != null ? newNote.getNotebook().getId() : null,
                newNote.getTags().stream().map(Tag::getId).toList()
        );
    }

    @Transactional(readOnly = true)
    public GetNoteResponse getNote(Long id) {
        Note foundNote = repository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
        return new GetNoteResponse(
                foundNote.getId(),
                foundNote.getTitle(),
                foundNote.getText(),
                foundNote.getNotebook() != null ? foundNote.getNotebook().getId() : null,
                foundNote.getTags().stream().map(Tag::getId).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<GetNoteResponse> getAllNotes() {
        return repository.findAll().stream()
                .map(note -> new GetNoteResponse(
                        note.getId(),
                        note.getTitle(),
                        note.getText(),
                        note.getNotebook() != null ? note.getNotebook().getId() : null,
                        note.getTags().stream().map(Tag::getId).toList()))
                .toList();
    }

    @Transactional
    public void updateNote(Long id, UpdateNoteRequest request) {
        Note note = repository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
        if (note.getTitle() == null || !note.getTitle().equals(request.title()))
            note.setTitle(request.title());
        if (note.getText() == null || !note.getText().equals(request.text()))
            note.setText(request.text());

        if (request.notebookId() != null) {
            Notebook notebook = notebookRepository.findById(request.notebookId()).orElseThrow(() -> new NotebookNotFoundException(request.notebookId()));
            note.setNotebook(notebook);
        }

        if (request.tags() != null) {
            List<Long> currentTagIds = note.getTags().stream().map(Tag::getId).toList();
            if (!currentTagIds.equals(request.tags())) {
                note.getTags().clear();
request.tags().forEach(tagId -> {
Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));
                    note.getTags().add(tag);
                });
            }
        }
    }

    @Transactional
    public void deleteNote(Long id) {
        Note note = repository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
        repository.delete(note);
    }
}