package com.tcyao.nid.note.service;

import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.identity.repository.UserRepository;
import com.tcyao.nid.note.dto.*;
import com.tcyao.nid.note.entity.Attachment;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.exception.NoteNotFoundException;
import com.tcyao.nid.note.exception.NotebookNotFoundException;
import com.tcyao.nid.note.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NotebookRepository repository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    @Transactional
    public CreateNoteResponse createNote(Long notebookId, CreateNoteRequest request, UUID userId) {
        Notebook notebook = requireNotebook(notebookId, userId);
        User creator = requireUser(userId);

        List<Attachment> attachments = new ArrayList<>();
        List<AttachmentResponse> responses = new ArrayList<>();
        for (AttachmentInput input : nullSafe(request.attachments())) {
            AttachmentService.Provisioned provisioned = attachmentService.provision(input, userId);
            attachments.add(provisioned.attachment());
            responses.add(toResponse(provisioned));
        }

        Note note = notebook.addNote(request.title(), request.text(), attachments, creator);
        repository.saveAndFlush(notebook);
        return new CreateNoteResponse(note.getId(), note.getTitle(), note.getText(), notebook.getId(), responses);
    }

    @Transactional
    public GetNoteResponse updateNote(Long notebookId, Long noteId, UpdateNoteRequest request, UUID userId) {
        Notebook notebook = requireNotebook(notebookId, userId);
        Note note = requireNote(notebook, noteId);
        User editor = requireUser(userId);

        List<Attachment> attachments = new ArrayList<>();
        List<AttachmentResponse> responses = new ArrayList<>();
        for (AttachmentInput input : nullSafe(request.attachments())) {
            AttachmentService.Provisioned provisioned = attachmentService.provision(input, userId);
            attachments.add(provisioned.attachment());
            responses.add(toResponse(provisioned));
        }

        notebook.updateNote(noteId, request.title(), request.text(), editor);
        notebook.replaceNoteAttachments(noteId, attachments);
        return new GetNoteResponse(note.getId(), note.getTitle(), note.getText(), notebook.getId(), responses);
    }

    @Transactional(readOnly = true)
    public List<GetNoteResponse> getNotes(Long notebookId, UUID userId) {
        Notebook notebook = requireNotebook(notebookId, userId);
        return notebook.getNotes().stream()
                .map(note -> toResponse(notebook, note))
                .toList();
    }

    @Transactional(readOnly = true)
    public GetNoteResponse getNote(Long notebookId, Long noteId, UUID userId) {
        Notebook notebook = requireNotebook(notebookId, userId);
        return toResponse(notebook, requireNote(notebook, noteId));
    }

    @Transactional
    public void deleteNote(Long notebookId, Long noteId, UUID userId) {
        Notebook notebook = requireNotebook(notebookId, userId);
        notebook.removeNote(noteId);
    }

    static List<AttachmentResponse> toAttachmentResponses(Note note) {
        return note.getAttachments().stream()
                .map(a -> new AttachmentResponse(a.getUploadId(), a.getFileName(), a.getContentType(), null))
                .toList();
    }

    private static AttachmentResponse toResponse(AttachmentService.Provisioned provisioned) {
        Attachment a = provisioned.attachment();
        return new AttachmentResponse(a.getUploadId(), a.getFileName(), a.getContentType(), provisioned.uploadUrl());
    }

    private GetNoteResponse toResponse(Notebook notebook, Note note) {
        return new GetNoteResponse(note.getId(), note.getTitle(), note.getText(), notebook.getId(), toAttachmentResponses(note));
    }

    private Note requireNote(Notebook notebook, Long noteId) {
        return notebook.getNotes().stream()
                .filter(n -> n.getId() != null && n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new NoteNotFoundException(noteId));
    }

    private Notebook requireNotebook(Long notebookId, UUID userId) {
        return repository.findByIdAndOwner_Id(notebookId, userId)
                .orElseThrow(() -> new NotebookNotFoundException(notebookId));
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(userId.toString()));
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
