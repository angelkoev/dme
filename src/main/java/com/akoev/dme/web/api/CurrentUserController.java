package com.akoev.dme.web.api;

import com.akoev.dme.domain.model.Role;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import com.akoev.dme.web.api.dto.CurrentUserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class CurrentUserController {

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getDomainUser();
        Set<String> roles = user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet());
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
