package com.akoev.dme.movies;

import com.akoev.dme.decisionengine.ScoringStrategy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class MovieScorer implements ScoringStrategy<MovieContext, Title> {

    private static final double BASE_SCORE = 50;
    private static final double PREFERRED_GENRE_BONUS = 20;
    private static final double VARIETY_JITTER_MAX = 5;

    @Override
    public double score(MovieContext context, Title candidate) {
        double score = BASE_SCORE;
        if (context.preferredGenres().contains(candidate.genre())) {
            score += PREFERRED_GENRE_BONUS;
        }
        score += ThreadLocalRandom.current().nextDouble(0, VARIETY_JITTER_MAX);
        return score;
    }
}
