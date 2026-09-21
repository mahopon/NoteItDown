package com.tcyao.nid.identity.dto;

import jakarta.validation.constraints.Email;

public record LoginUserRequest(
        @Email String email,
        String password
) {
}
