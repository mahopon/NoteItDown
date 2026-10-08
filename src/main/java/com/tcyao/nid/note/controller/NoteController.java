package com.tcyao.nid.note.controller;

import com.tcyao.nid.identity.dto.UserPrincipal;
import com.tcyao.nid.note.dto.CreateNoteRequest;
import com.tcyao.nid.note.dto.CreateNoteResponse;
import com.tcyao.nid.note.dto.GetNoteResponse;
import com.tcyao.nid.note.dto.UpdateNoteRequest;
import com.tcyao.nid.note.service.AttachmentService;
import com.tcyao.nid.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notebooks/{notebookId}/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    private final AttachmentService attachmentService;

    @PostMapping("")
    public ResponseEntity<CreateNoteResponse> createNote(
            @PathVariable Long notebookId,
            @Valid @RequestBody CreateNoteRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        CreateNoteResponse response = noteService.createNote(notebookId, request, principal.getId());
        URI location = URI.create("/notebooks/" + notebookId + "/notes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("")
    public ResponseEntity<List<GetNoteResponse>> getAllNotes(
            @PathVariable Long notebookId,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(noteService.getNotes(notebookId, principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetNoteResponse> getNote(
            @PathVariable Long notebookId,
            @PathVariable Long id,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(noteService.getNote(notebookId, id, principal.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetNoteResponse> updateNote(
            @PathVariable Long notebookId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(noteService.updateNote(notebookId, id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long notebookId,
            @PathVariable Long id,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        noteService.deleteNote(notebookId, id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attachments/{uploadId}")
    public ResponseEntity<Void> downloadAttachment(
            @PathVariable Long notebookId,
            @PathVariable Long id,
            @PathVariable UUID uploadId,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String url = attachmentService.downloadUrl(notebookId, id, uploadId, principal.getId());
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
}
