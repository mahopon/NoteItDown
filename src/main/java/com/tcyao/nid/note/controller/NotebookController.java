package com.tcyao.nid.note.controller;

import com.tcyao.nid.identity.dto.UserPrincipal;
import com.tcyao.nid.note.dto.CreateNotebookRequest;
import com.tcyao.nid.note.dto.CreateNotebookResponse;
import com.tcyao.nid.note.dto.GetNotebookResponse;
import com.tcyao.nid.note.dto.UpdateNotebookRequest;
import com.tcyao.nid.note.service.NotebookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/notebooks")
@RequiredArgsConstructor
public class NotebookController {
    private final NotebookService notebookService;

    @PostMapping("")
    public ResponseEntity<CreateNotebookResponse> createNotebook(
            @Valid @RequestBody CreateNotebookRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        CreateNotebookResponse response = notebookService.createNotebook(request, principal.getId());
        URI location = URI.create("/notebooks/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("")
    public ResponseEntity<List<GetNotebookResponse>> getAllNotebooks(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(notebookService.getAllNotebooks(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetNotebookResponse> getNotebook(@PathVariable Long id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(notebookService.getNotebook(id, principal.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateNotebook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotebookRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        notebookService.updateNotebook(id, principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotebook(@PathVariable Long id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        notebookService.deleteNotebook(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}