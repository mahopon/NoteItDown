package com.tcyao.nid.identity.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityRabbitConfig {

    public static final String IDENTITY_EXCHANGE = "identity.exchange";
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";

    @Bean
    DirectExchange identityExchange() {
        return new DirectExchange(IDENTITY_EXCHANGE, true, false);
    }

    @Bean
    CommandLineRunner testRabbit(RabbitTemplate rabbitTemplate) {
        return args -> {
            try {
                rabbitTemplate.execute(channel -> {
                    System.out.println("RabbitMQ connection OK: " + channel.getConnection());
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}