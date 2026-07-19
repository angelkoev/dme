package com.akoev.dme.web.api;

import com.akoev.dme.movies.MovieContext;
import com.akoev.dme.movies.MovieRecommendationService;
import com.akoev.dme.movies.Title;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieRecommendationService movieRecommendationService;

    @GetMapping
    public List<Title> titles() {
        return movieRecommendationService.listTitles();
    }

    @PostMapping("/recommend")
    public List<Title> recommend(@RequestBody MovieContext context) {
        return movieRecommendationService.recommend(context);
    }
}
