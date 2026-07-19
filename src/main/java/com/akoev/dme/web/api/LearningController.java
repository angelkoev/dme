package com.akoev.dme.web.api;

import com.akoev.dme.learning.Course;
import com.akoev.dme.learning.LearningContext;
import com.akoev.dme.learning.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningPathService learningPathService;

    @GetMapping("/courses")
    public List<Course> courses() {
        return learningPathService.listCourses();
    }

    @PostMapping("/recommend")
    public List<Course> recommend(@Valid @RequestBody LearningContext context) {
        return learningPathService.recommend(context);
    }
}
