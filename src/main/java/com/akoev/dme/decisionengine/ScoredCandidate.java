package com.akoev.dme.decisionengine;

public record ScoredCandidate<T>(T candidate, double score) {
}
