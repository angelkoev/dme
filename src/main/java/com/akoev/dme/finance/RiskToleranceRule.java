package com.akoev.dme.finance;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

@Component
public class RiskToleranceRule implements Rule<FinanceContext, Instrument> {

    @Override
    public boolean isSatisfiedBy(FinanceContext context, Instrument candidate) {
        return rank(candidate.riskLevel()) <= rank(context.riskTolerance());
    }

    @Override
    public String description() {
        return "Instrument risk level must not exceed the user's risk tolerance";
    }

    // Explicit ranking rather than .ordinal() across two independently
    // declared enums — same rationale as fitness's ExperienceLevelRule.
    private static int rank(RiskLevel level) {
        return switch (level) {
            case LOW -> 0;
            case MEDIUM -> 1;
            case HIGH -> 2;
        };
    }

    private static int rank(RiskTolerance tolerance) {
        return switch (tolerance) {
            case CONSERVATIVE -> 0;
            case BALANCED -> 1;
            case AGGRESSIVE -> 2;
        };
    }
}
