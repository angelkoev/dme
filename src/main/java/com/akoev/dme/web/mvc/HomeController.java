package com.akoev.dme.web.mvc;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    // Basic (thin, no persisted profile/history) but live — each is a real
    // Rule/ScoringStrategy instantiation of the generic engine, just much
    // smaller in scope than fitness. Add a no-url DecisionDomainCard here
    // for a future domain that isn't built yet; nothing else about the home
    // page needs to change either way.
    private static final List<DecisionDomainCard> DOMAINS = List.of(
            new DecisionDomainCard("Portfolio Advisor",
                    "Investment picks based on risk tolerance, time horizon, and sector preferences.", "/finance"),
            new DecisionDomainCard("Meal Planner",
                    "A day's meal plan based on your diet goal and allergies.", "/meals"),
            new DecisionDomainCard("Movie & Show Picks",
                    "Recommendations based on genre preferences and available time.", "/movies"),
            new DecisionDomainCard("Learning Path",
                    "Course picks for a skill area, capped one level above where you are today.", "/learning"),
            new DecisionDomainCard("Daily Task Prioritizer",
                    "Ranks your own tasks by urgency, importance, and energy level — the one domain here that ranks what you type in, not a fixed catalog.", "/tasks")
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
        model.addAttribute("domains", DOMAINS);
        return "home";
    }
}
