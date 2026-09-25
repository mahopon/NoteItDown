package com.tcyao.nid.common.infra.mail;

public interface EmailSender {
    void send(EmailMessage message);
}
