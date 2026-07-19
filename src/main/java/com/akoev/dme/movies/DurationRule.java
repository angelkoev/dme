package com.akoev.dme.movies;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

@Component
public class DurationRule implements Rule<MovieContext, Title> {

    @Override
    public boolean isSatisfiedBy(MovieContext context, Title candidate) {
        return candidate.durationMinutes() <= context.maxDurationMinutes();
    }

    @Override
    public String description() {
        return "Title must fit within the user's available viewing time";
    }
}
