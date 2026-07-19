package com.akoev.dme.productivity;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

@Component
public class FitsAvailableTimeRule implements Rule<ProductivityContext, Task> {

    @Override
    public boolean isSatisfiedBy(ProductivityContext context, Task candidate) {
        return candidate.estimatedMinutes() <= context.availableMinutesToday();
    }

    @Override
    public String description() {
        return "Task must fit within the time actually available today";
    }
}
