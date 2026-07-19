package com.akoev.dme.movies;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MovieRecommendationTest {

    private final MovieScorer scorer = new MovieScorer();

    @Test
    void preferredGenreScoresHigherThanNonPreferred() {
        MovieContext context = new MovieContext(Set.of(Genre.COMEDY), Set.of(), 180);
        Title comedy = new Title(1L, "A", Genre.COMEDY, 100);
        Title drama = new Title(2L, "B", Genre.DRAMA, 100);

        assertThat(scorer.score(context, comedy)).isGreaterThan(scorer.score(context, drama));
    }

    @Test
    void durationRuleExcludesTitleLongerThanAvailableTime() {
        DurationRule rule = new DurationRule();
        MovieContext context = new MovieContext(Set.of(), Set.of(), 90);
        Title tooLong = new Title(1L, "A", Genre.ACTION, 130);

        assertThat(rule.isSatisfiedBy(context, tooLong)).isFalse();
    }

    @Test
    void excludedGenreRuleExcludesThatGenre() {
        ExcludedGenreRule rule = new ExcludedGenreRule();
        MovieContext context = new MovieContext(Set.of(), Set.of(Genre.HORROR), 180);
        Title horror = new Title(1L, "A", Genre.HORROR, 100);

        assertThat(rule.isSatisfiedBy(context, horror)).isFalse();
    }

    @Test
    void recommendExcludesTitlesOverTimedBudget() {
        TitleCatalog catalog = new TitleCatalog();
        MovieRecommendationService service = new MovieRecommendationService(
                catalog, List.of(new ExcludedGenreRule(), new DurationRule()), new MovieScorer());

        MovieContext context = new MovieContext(Set.of(), Set.of(), 95);
        List<Title> recommendations = service.recommend(context);

        assertThat(recommendations).allMatch(title -> title.durationMinutes() <= 95);
    }
}
