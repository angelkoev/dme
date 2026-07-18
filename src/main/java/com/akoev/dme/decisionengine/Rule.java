package com.akoev.dme.decisionengine;

/**
 * A hard admissibility check: a candidate that fails any rule is excluded
 * entirely, regardless of score. Domain-agnostic by design — {@code C} is
 * whatever context a concrete decision domain needs (e.g. a fitness user
 * profile), {@code T} is the type of thing being decided between.
 */
public interface Rule<C, T> {

    boolean isSatisfiedBy(C context, T candidate);

    String description();
}
