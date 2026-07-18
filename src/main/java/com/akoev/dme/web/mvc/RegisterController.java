package com.akoev.dme.web.mvc;

import com.akoev.dme.application.service.AuthService;
import com.akoev.dme.web.api.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final AuthService authService;

    @GetMapping("/register")
    public String form(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest("", "", ""));
        }
        return "register";
    }

    @PostMapping("/register")
    public String submit(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            authService.register(request.username(), request.email(), request.password());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
