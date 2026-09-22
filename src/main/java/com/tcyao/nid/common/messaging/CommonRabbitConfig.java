package com.tcyao.nid.common.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonRabbitConfig {
    public static final String COMMON_DLX = "common.dlx";

    @Bean
    DirectExchange dlExchange() {
        return new DirectExchange(COMMON_DLX);
    }

}
