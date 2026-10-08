package com.tcyao.nid.common.infra.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Profile("dev")
    @Bean
    public S3Client s3Client(
            @Value("${S3_ENDPOINT}") String endpoint,
            @Value("${S3_ACCESS_KEY}") String accessKey,
            @Value("${S3_SECRET_KEY}") String secretKey
    ) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        accessKey,
                                        secretKey
                                )
                        )
                )
                .build();
    }

    @Profile("aws")
    @Bean
    public S3Client s3ClientAWS() {
        return S3Client.builder()
                .region(Region.of("ap-southeast-1"))
                .build();
    }

    @Profile("dev")
    @Bean
    public S3Presigner s3Presigner(
            @Value("${S3_ENDPOINT}") String endpoint,
            @Value("${S3_ACCESS_KEY}") String accessKey,
            @Value("${S3_SECRET_KEY}") String secretKey
    ) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        accessKey,
                                        secretKey
                                )
                        )
                )
                .build();
    }

    @Profile("aws")
    @Bean
    public S3Presigner s3PresignerAWS() {
        return S3Presigner.builder()
                .region(Region.of("ap-southeast-1"))
                .build();
    }
}