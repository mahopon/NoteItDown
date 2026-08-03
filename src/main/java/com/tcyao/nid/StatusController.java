package com.tcyao.nid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("/status")
public class StatusController {
    @GetMapping("")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Up!");
    }
}
