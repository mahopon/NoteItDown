package com.tcyao.nid.common.infra.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Component
@ConditionalOnProperty(name = "nid.mail.transport", havingValue = "ses")
public class SesEmailSender implements EmailSender {

    private final SesV2Client sesV2Client;
    private final String from;

    public SesEmailSender(SesV2Client sesV2Client, @Value("${nid.mail.from}") String from) {
        this.sesV2Client = sesV2Client;
        this.from = from;
    }

    @Override
    public void send(EmailMessage m) {
        sesV2Client.sendEmail(SendEmailRequest
                .builder()
                .fromEmailAddress(from)
                .destination(Destination.builder().toAddresses(m.to()).build())
                .content(EmailContent.builder()
                        .simple(Message.builder()
                                .subject(Content.builder().data(m.subject()).build())
                                .body(Body.builder()
                                        .text(Content.builder().data(m.body()).build())
                                        .build())
                                .build())
                        .build())
                .build());
    }
}
