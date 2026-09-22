package com.tcyao.nid.identity.messaging.event;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId) {
}