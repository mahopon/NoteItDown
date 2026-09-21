package com.tcyao.nid.note.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/artifact")
@RequiredArgsConstructor
public class ArtifactController {


    @PostMapping(value = "",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadArtifact(@RequestPart("file") List<MultipartFile> files) {
        for (MultipartFile file: files) {
            System.out.println(file.getContentType());
        }

        return ResponseEntity.ok().build();
    }
}
