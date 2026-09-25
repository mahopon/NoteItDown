package com.tcyao.nid.common.infra.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "nid.mail.transport", havingValue = "ses")
public class SesConfig {
    @Bean
    SesV2Client sesV2Client(@Value("${nid.mail.ses.region}") String region, @Value("${SES_ENDPOINT:}") String endpoint) {
        var builder = SesV2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (!endpoint.isBlank()) builder.endpointOverride(URI.create(endpoint));
        return builder.build();
    }
}
