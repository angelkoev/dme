package com.akoev.dme.web.mvc;

import com.akoev.dme.application.service.UserProfileService;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public String view(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        try {
            model.addAttribute("profile", userProfileService.getProfile(principal.getId()));
        } catch (ResponseStatusException ex) {
            model.addAttribute("noProfile", true);
        }
        return "profile";
    }
}
