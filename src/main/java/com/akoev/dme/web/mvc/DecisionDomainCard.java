package com.akoev.dme.web.mvc;

/**
 * One card in the home page's domain grid. {@code url == null} means "not
 * built yet" (rendered with a "Coming soon" badge, no link); otherwise it's
 * a live domain and the card links straight to its page. Workout Planner is
 * the one exception — its card renders its full nav directly in home.html
 * rather than a single link, since it has several pages, not one.
 * <p>
 * {@code decisionengine} (Rule/ScoringStrategy/RuleBasedDecisionEngine) has
 * zero fitness-specific code, so any of these plug in the same way
 * {@code fitness.engine} does, with their own context/candidate types and
 * rules/scoring — see the {@code finance}/{@code meals}/{@code movies}/
 * {@code learning}/{@code productivity} packages for the (deliberately
 * thin) shape each one follows.
 */
public record DecisionDomainCard(String name, String description, String url) {

    public DecisionDomainCard(String name, String description) {
        this(name, description, null);
    }

    public boolean isLive() {
        return url != null;
    }
}
