package com.tcyao.nid.identity.dto;

import java.util.UUID;

public record CreateUserResponse(
        UUID id,
        String email
) {
}
