package com.tcyao.nid.note.service;

import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.identity.repository.UserRepository;
import com.tcyao.nid.note.dto.*;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotebookService {
    private final NotebookRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public CreateNotebookResponse createNotebook(CreateNotebookRequest request, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException(ownerId.toString()));
        Notebook notebook = new Notebook();
        notebook.setTitle(request.title());
        notebook.setKind(request.kind());
        notebook.setOwner(owner);
        repository.save(notebook);
        return new CreateNotebookResponse(notebook.getId(), notebook.getTitle(), notebook.getCreatedAt(), notebook.getModifiedAt());
    }

    @Transactional(readOnly = true)
    public GetNotebookResponse getNotebook(Long id, UUID ownerId) {
        Notebook notebook = repository.findByIdAndOwner_Id(id, ownerId).orElseThrow(() -> new NotebookNotFoundException(id));
        List<GetNoteResponse> notes = notebook.getNotes().stream()
                .map(n -> new GetNoteResponse(n.getId(), n.getTitle(), n.getText(),
                        notebook.getId()))
                .toList();
        return new GetNotebookResponse(notebook.getId(), notebook.getTitle(), notes, notebook.getCreatedAt(), notebook.getModifiedAt());
    }

    @Transactional(readOnly = true)
    public List<GetNotebookResponse> getAllNotebooks(UUID ownerId) {
        return repository.findByOwner_Id(ownerId).stream()
                .map(nb -> {
                    List<GetNoteResponse> notes = nb.getNotes().stream()
                            .map(n -> new GetNoteResponse(n.getId(), n.getTitle(), n.getText(),
                                    nb.getId()))
                            .toList();
                    return new GetNotebookResponse(nb.getId(), nb.getTitle(), notes, nb.getCreatedAt(), nb.getModifiedAt());
                })
                .toList();
    }

    @Transactional
    public void updateNotebook(Long id, UUID ownerId, UpdateNotebookRequest request) {
        Notebook notebook = repository.findByIdAndOwner_Id(id, ownerId).orElseThrow(() -> new NotebookNotFoundException(id));
        notebook.setTitle(request.title());
        notebook.setModifiedAt(java.time.Instant.now());
    }

    @Transactional
    public void deleteNotebook(Long id, UUID ownerId) {
        Notebook notebook = repository.findByIdAndOwner_Id(id, ownerId).orElseThrow(() -> new NotebookNotFoundException(id));
        repository.delete(notebook);
    }
}