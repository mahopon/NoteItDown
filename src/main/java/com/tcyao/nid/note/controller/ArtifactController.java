package com.tcyao.nid.note.controller;

import com.tcyao.nid.identity.dto.UserPrincipal;
import com.tcyao.nid.note.service.ArtifactService;
import com.tcyao.nid.storage.dto.CreateArtifactRequest;
import com.tcyao.nid.storage.dto.CreateArtifactResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/artifact")
@RequiredArgsConstructor
public class ArtifactController {

    private final ArtifactService artifactService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateArtifactResponse> createArtifact(
            @Valid @RequestBody CreateArtifactRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(
                artifactService.createArtifact(request, principal.getId())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> getArtifact(
            @PathVariable UUID id,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String downloadUrl = artifactService.getArtifactDownloadUrl(id, principal.getId());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(downloadUrl))
                .build();
    }
}
