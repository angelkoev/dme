package com.akoev.dme.web.mvc;

import com.akoev.dme.learning.LearningContext;
import com.akoev.dme.learning.LearningPathService;
import com.akoev.dme.learning.SkillArea;
import com.akoev.dme.learning.SkillLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LearningViewController {

    private final LearningPathService learningPathService;

    @GetMapping("/learning")
    public String view(Model model) {
        model.addAttribute("courses", learningPathService.listCourses());
        model.addAttribute("skillAreas", SkillArea.values());
        model.addAttribute("skillLevels", SkillLevel.values());
        return "learning";
    }

    @PostMapping("/learning/recommend")
    public String recommend(@RequestParam SkillArea targetSkillArea,
                             @RequestParam SkillLevel currentLevel,
                             Model model) {
        LearningContext context = new LearningContext(targetSkillArea, currentLevel);
        model.addAttribute("recommendations", learningPathService.recommend(context));
        model.addAttribute("courses", learningPathService.listCourses());
        model.addAttribute("skillAreas", SkillArea.values());
        model.addAttribute("skillLevels", SkillLevel.values());
        return "learning";
    }
}
