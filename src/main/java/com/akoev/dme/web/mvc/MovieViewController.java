package com.akoev.dme.web.mvc;

import com.akoev.dme.movies.Genre;
import com.akoev.dme.movies.MovieContext;
import com.akoev.dme.movies.MovieRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class MovieViewController {

    private static final int DEFAULT_MAX_DURATION_MINUTES = 180;

    private final MovieRecommendationService movieRecommendationService;

    @GetMapping("/movies")
    public String view(Model model) {
        model.addAttribute("titles", movieRecommendationService.listTitles());
        model.addAttribute("genres", Genre.values());
        return "movies";
    }

    @PostMapping("/movies/recommend")
    public String recommend(@RequestParam(required = false) Set<Genre> preferredGenres,
                             @RequestParam(required = false) Set<Genre> excludedGenres,
                             @RequestParam(defaultValue = "" + DEFAULT_MAX_DURATION_MINUTES) int maxDurationMinutes,
                             Model model) {
        MovieContext context = new MovieContext(preferredGenres, excludedGenres, maxDurationMinutes);
        model.addAttribute("recommendations", movieRecommendationService.recommend(context));
        model.addAttribute("titles", movieRecommendationService.listTitles());
        model.addAttribute("genres", Genre.values());
        return "movies";
    }
}
