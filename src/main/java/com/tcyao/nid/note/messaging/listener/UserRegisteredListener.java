package com.tcyao.nid.note.messaging.listener;

import com.tcyao.nid.note.messaging.event.UserRegisteredEvent;
import com.tcyao.nid.note.dto.CreateNotebookRequest;
import com.tcyao.nid.note.enums.NotebookKind;
import com.tcyao.nid.note.messaging.NoteRabbitConfig;
import com.tcyao.nid.note.service.NotebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {
    private final NotebookService notebookService;
    @RabbitListener(queues = NoteRabbitConfig.NOTEBOOK_CREATION_QUEUE)
    public void handle (UserRegisteredEvent event) {
        notebookService.createNotebook(new CreateNotebookRequest("Default", NotebookKind.DEFAULT), event.userId());
    }
}
