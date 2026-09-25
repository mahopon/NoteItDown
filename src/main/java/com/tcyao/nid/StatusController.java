package com.tcyao.nid;

import com.tcyao.nid.common.infra.mail.EmailMessage;
import com.tcyao.nid.common.infra.mail.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/status")
public class StatusController {

    private final EmailSender emailSender;
    private final String transport;

    public StatusController(EmailSender emailSender,
                            @Value("${nid.mail.transport}") String transport) {
        this.emailSender = emailSender;
        this.transport = transport;
    }

    @GetMapping("")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Up!");
    }

    @GetMapping("/mail")
    public ResponseEntity<String> sendTestMail(
            @RequestParam(defaultValue = "test@example.com") String to,
            @RequestParam(defaultValue = "NID test email") String subject,
            @RequestParam(defaultValue = "This is a test email from NID.") String body) {
        emailSender.send(new EmailMessage(to, subject, body + " (sent at " + Instant.now() + ")"));
        return ResponseEntity.ok("Sent via " + transport + " to " + to);
    }
}
