package com.akoev.dme.finance;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

@Component
public class ExcludedSectorRule implements Rule<FinanceContext, Instrument> {

    @Override
    public boolean isSatisfiedBy(FinanceContext context, Instrument candidate) {
        return !context.excludedSectors().contains(candidate.sector());
    }

    @Override
    public String description() {
        return "Instrument must not be in a sector the user excluded";
    }
}
