package com.tcyao.nid.note.controller;

import com.tcyao.nid.note.dto.CreateNotebookRequest;
import com.tcyao.nid.note.dto.CreateNotebookResponse;
import com.tcyao.nid.note.dto.GetNotebookResponse;
import com.tcyao.nid.note.dto.UpdateNotebookRequest;
import com.tcyao.nid.note.service.NotebookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/notebooks")
@RequiredArgsConstructor
public class NotebookController {
    private final NotebookService notebookService;

    @PostMapping("")
    public ResponseEntity<CreateNotebookResponse> createNotebook(@Valid @RequestBody CreateNotebookRequest request) {
        CreateNotebookResponse response = notebookService.createNotebook(request);
        URI location = URI.create("/notebooks/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("")
    public ResponseEntity<List<GetNotebookResponse>> getAllNotebooks() {
        return ResponseEntity.ok(notebookService.getAllNotebooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetNotebookResponse> getNotebook(@PathVariable Long id) {
        return ResponseEntity.ok(notebookService.getNotebook(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateNotebook(@PathVariable Long id, @Valid @RequestBody UpdateNotebookRequest request) {
        notebookService.updateNotebook(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotebook(@PathVariable Long id) {
        notebookService.deleteNotebook(id);
        return ResponseEntity.noContent().build();
    }
}