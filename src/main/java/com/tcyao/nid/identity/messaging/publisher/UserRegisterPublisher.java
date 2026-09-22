package com.tcyao.nid.identity.messaging.publisher;

import com.tcyao.nid.common.messaging.EventPublisher;
import com.tcyao.nid.identity.messaging.IdentityRabbitConfig;
import com.tcyao.nid.identity.messaging.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterPublisher implements EventPublisher<UserRegisteredEvent> {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(UserRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
                IdentityRabbitConfig.IDENTITY_EXCHANGE,
                IdentityRabbitConfig.USER_REGISTERED_ROUTING_KEY,
                event
        );
    }
}
