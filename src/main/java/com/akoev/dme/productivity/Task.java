package com.akoev.dme.productivity;

/**
 * The candidate ({@code T}) in this domain's instantiation of the generic
 * engine — unlike every other domain here (and unlike fitness), the
 * candidates are supplied by the caller on each request, not drawn from a
 * catalog. This is deliberately the odd one out: it proves the engine
 * generalizes to "rank the user's own items" just as well as "pick from a
 * fixed pool."
 */
public record Task(String name, Urgency urgency, Importance importance, int estimatedMinutes) {
}
