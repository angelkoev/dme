package com.akoev.dme.movies;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

@Component
public class ExcludedGenreRule implements Rule<MovieContext, Title> {

    @Override
    public boolean isSatisfiedBy(MovieContext context, Title candidate) {
        return !context.excludedGenres().contains(candidate.genre());
    }

    @Override
    public String description() {
        return "Title must not be in a genre the user excluded";
    }
}
