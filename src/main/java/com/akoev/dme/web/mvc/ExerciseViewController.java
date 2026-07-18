package com.akoev.dme.web.mvc;

import com.akoev.dme.application.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ExerciseViewController {

    private final ExerciseService exerciseService;

    @GetMapping("/exercises")
    public String list(Model model) {
        model.addAttribute("exercises", exerciseService.listAll());
        return "exercises";
    }
}
