package com.tcyao.nid.note.service;

import com.tcyao.nid.note.dto.*;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.entity.Tag;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotebookService {
    private final NotebookRepository repository;

    public CreateNotebookResponse createNotebook(CreateNotebookRequest request) {
        Notebook notebook = new Notebook();
        notebook.setTitle(request.title());
        repository.save(notebook);
        return new CreateNotebookResponse(notebook.getId(), notebook.getTitle(), notebook.getCreatedAt(), notebook.getModifiedAt());
    }

    @Transactional(readOnly = true)
    public GetNotebookResponse getNotebook(Long id) {
        Notebook notebook = repository.findById(id).orElseThrow(() -> new NotebookNotFoundException(id));
        List<GetNoteResponse> notes = notebook.getNotes().stream()
                .map(n -> new GetNoteResponse(n.getId(), n.getTitle(), n.getText(),
                        notebook.getId(), n.getTags().stream().map(Tag::getId).toList()))
                .toList();
        return new GetNotebookResponse(notebook.getId(), notebook.getTitle(), notes, notebook.getCreatedAt(), notebook.getModifiedAt());
    }

    @Transactional(readOnly = true)
    public List<GetNotebookResponse> getAllNotebooks() {
        return repository.findAll().stream()
                .map(nb -> {
                    List<GetNoteResponse> notes = nb.getNotes().stream()
                            .map(n -> new GetNoteResponse(n.getId(), n.getTitle(), n.getText(),
                                    nb.getId(), n.getTags().stream().map(Tag::getId).toList()))
                            .toList();
                    return new GetNotebookResponse(nb.getId(), nb.getTitle(), notes, nb.getCreatedAt(), nb.getModifiedAt());
                })
                .toList();
    }

    @Transactional
    public void updateNotebook(Long id, UpdateNotebookRequest request) {
        Notebook notebook = repository.findById(id).orElseThrow(() -> new NotebookNotFoundException(id));
        notebook.setTitle(request.title());
        notebook.setModifiedAt(java.time.Instant.now());
    }

    @Transactional
    public void deleteNotebook(Long id) {
        Notebook notebook = repository.findById(id).orElseThrow(() -> new NotebookNotFoundException(id));
        repository.delete(notebook);
    }
}