package com.tcyao.nid.identity.dto;

import jakarta.validation.constraints.Email;

import java.util.UUID;

public record GetUserRequest(
        UUID id
) {
}
