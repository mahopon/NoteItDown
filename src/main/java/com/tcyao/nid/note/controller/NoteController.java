package com.tcyao.nid.note.controller;

import com.tcyao.nid.identity.dto.UserPrincipal;
import com.tcyao.nid.note.dto.CreateNoteRequest;
import com.tcyao.nid.note.dto.CreateNoteResponse;
import com.tcyao.nid.note.dto.GetNoteResponse;
import com.tcyao.nid.note.dto.UpdateNoteRequest;
import com.tcyao.nid.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping("")
    public ResponseEntity<CreateNoteResponse> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        CreateNoteResponse response = noteService.createNote(request, principal.getId());
        URI location = URI.create("/notes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("")
    public ResponseEntity<List<GetNoteResponse>> getAllNotes(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(noteService.getAllNotes(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetNoteResponse> getNote(@PathVariable Long id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok((noteService.getNote(id, principal.getId())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        noteService.updateNote(id, principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        noteService.deleteNote(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}