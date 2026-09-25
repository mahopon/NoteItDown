package com.tcyao.nid.common.infra.mail;

public record EmailMessage(String to, String subject, String body) {
}
