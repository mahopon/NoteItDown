package com.tcyao.nid.note.controllers;

import com.tcyao.nid.note.dto.NotePresencePayload;
import com.tcyao.nid.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NoteWSController {
    private final NoteService noteService;

    @MessageMapping("/note/{id}/join")
    public void joinNotePresence(@DestinationVariable long id) {

    }

    @MessageMapping("/note/{id}/exit")
    public void exitNotePresence(@DestinationVariable long id) {

    }

    @MessageMapping("/note/{id}/presence")
    public void updatePresence(@DestinationVariable long id, @Valid @Payload NotePresencePayload payload) {

    }

    @MessageMapping("/note/{id}/edit")
    public void updateNote() {

    }
    @MessageMapping("/test/{message}")
    public void test(@DestinationVariable String message) {
        System.out.println(message);
    }
}
