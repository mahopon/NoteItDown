package com.tcyao.nid.note.messaging;

import com.tcyao.nid.common.messaging.CommonRabbitConfig;
import com.tcyao.nid.identity.messaging.IdentityRabbitConfig;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoteRabbitConfig {

    public static final String NOTEBOOK_CREATION_QUEUE = "notebook.creation.queue";
    public static final String NOTEBOOK_CREATION_DLQ = "nmtebook.creation.dlq";

    @Bean
    Queue notebookCreationQueue() {
        return QueueBuilder
                .durable(NOTEBOOK_CREATION_QUEUE)
                .deadLetterExchange(CommonRabbitConfig.COMMON_DLX)
                .deadLetterRoutingKey(NOTEBOOK_CREATION_QUEUE)
                .build();
    }

    @Bean
    Binding notebookCreationBinding(@Qualifier("notebookCreationQueue") Queue notebookCreationQueue,
                                    @Qualifier("identityExchange") DirectExchange identityExchange) {
        return BindingBuilder.bind(notebookCreationQueue)
                .to(identityExchange)
                .with(IdentityRabbitConfig.USER_REGISTERED_ROUTING_KEY);
    }

    @Bean
    Queue notebookCreationDlq() {
        return QueueBuilder.durable(NOTEBOOK_CREATION_DLQ).build();
    }

    @Bean
    Binding notebookCreationDlqBinding(Queue notebookCreationDlq, @Qualifier("dlExchange") DirectExchange dlExchange) {
        return BindingBuilder.bind(notebookCreationDlq).to(dlExchange).with(NOTEBOOK_CREATION_QUEUE);
    }
}