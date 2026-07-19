package com.akoev.dme.web.mvc;

import com.akoev.dme.application.service.AuthService;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import com.akoev.dme.web.api.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class AccountViewController {

    private final AuthService authService;

    @GetMapping("/account/password")
    public String form(Model model) {
        if (!model.containsAttribute("changePasswordRequest")) {
            model.addAttribute("changePasswordRequest", new ChangePasswordRequest("", ""));
        }
        return "account-password";
    }

    @PostMapping("/account/password")
    public String submit(@AuthenticationPrincipal CustomUserDetails principal,
                          @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "account-password";
        }
        try {
            authService.changePassword(principal.getId(), request.currentPassword(), request.newPassword());
        } catch (ResponseStatusException ex) {
            model.addAttribute("passwordError", ex.getReason());
            return "account-password";
        }
        model.addAttribute("passwordChanged", true);
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest("", ""));
        return "account-password";
    }
}
