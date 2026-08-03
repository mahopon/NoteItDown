package com.tcyao.nid.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Client listens to anything with /topic prefix, meant for in-memory message broker
        config.setApplicationDestinationPrefixes("/client"); // Anything sent prefixed with /client is meant for @MessageMapping (stripped prefix)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // Connection point, all clients connect to /ws and use STOMP protocol
                .setAllowedOriginPatterns("*");
        // .withSockJS(); // Optional
    }
}