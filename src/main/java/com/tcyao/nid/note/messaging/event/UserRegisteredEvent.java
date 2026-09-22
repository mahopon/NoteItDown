package com.tcyao.nid.note.messaging.event;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId) {
}