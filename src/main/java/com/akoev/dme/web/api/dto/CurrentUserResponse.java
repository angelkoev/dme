package com.akoev.dme.web.api.dto;

import java.util.Set;

public record CurrentUserResponse(Long id, String username, String email, Set<String> roles) {
}
