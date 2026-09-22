package com.tcyao.nid.identity.messaging.handler;

import com.tcyao.nid.common.messaging.EventPublisher;
import com.tcyao.nid.identity.messaging.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegisteredHandler {
    private final EventPublisher<UserRegisteredEvent> publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserRegisteredEvent event) {
        publisher.publish(event);
    }
}
