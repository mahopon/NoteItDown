package com.tcyao.nid.identity.dto;

import java.util.UUID;

public record GetUserResponse(
        UUID uuid,
        String email
) {
}
