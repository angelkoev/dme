package com.akoev.dme.movies;

import java.util.Set;

public record MovieContext(Set<Genre> preferredGenres, Set<Genre> excludedGenres, int maxDurationMinutes) {

    public MovieContext {
        preferredGenres = preferredGenres == null ? Set.of() : preferredGenres;
        excludedGenres = excludedGenres == null ? Set.of() : excludedGenres;
    }
}
