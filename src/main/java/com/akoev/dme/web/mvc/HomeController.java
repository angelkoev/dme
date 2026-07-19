package com.akoev.dme.web.mvc;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    // Not built yet — see DecisionDomainCard. Update this list as new
    // domains land; nothing else about the home page needs to change.
    private static final List<DecisionDomainCard> COMING_SOON_DOMAINS = List.of(
            new DecisionDomainCard("Portfolio Advisor",
                    "Investment picks based on risk tolerance, time horizon, and sector preferences."),
            new DecisionDomainCard("Meal Planner",
                    "Meal plans based on dietary goals, allergies, and disliked ingredients."),
            new DecisionDomainCard("Movie & Show Picks",
                    "Recommendations based on genre preferences and what you've already watched."),
            new DecisionDomainCard("Learning Path",
                    "Course/skill recommendations based on a career goal, current level, and prerequisites.")
    );

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("username", authentication.getName());
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        }
        model.addAttribute("comingSoonDomains", COMING_SOON_DOMAINS);
        return "home";
    }
}
