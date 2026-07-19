package com.akoev.dme.productivity;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.decisionengine.RuleBasedDecisionEngine;
import com.akoev.dme.decisionengine.ScoredCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskPrioritizerService {

    private final List<Rule<ProductivityContext, Task>> rules;
    private final TaskScorer scorer;

    public List<Task> prioritize(ProductivityContext context, List<Task> tasks) {
        RuleBasedDecisionEngine<ProductivityContext, Task> engine = new RuleBasedDecisionEngine<>(rules, scorer);
        return engine.rank(context, tasks).stream().map(ScoredCandidate::candidate).toList();
    }
}
