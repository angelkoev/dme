package com.akoev.dme.web.api;

import com.akoev.dme.finance.FinanceAdvisorService;
import com.akoev.dme.finance.FinanceContext;
import com.akoev.dme.finance.Instrument;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public and stateless — unlike the fitness API, nothing here needs a JWT,
 * because nothing is persisted per-user (see FinanceContext). The
 * request/response bodies are the domain's own Context/candidate records
 * directly, not separate web DTOs: there's no persisted entity to keep the
 * web layer decoupled from here, so the extra indirection fitness has
 * (UpdateProfileRequest -> ProfileUpdateCommand) wouldn't buy anything.
 */
@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceAdvisorService financeAdvisorService;

    @GetMapping("/instruments")
    public List<Instrument> instruments() {
        return financeAdvisorService.listInstruments();
    }

    @PostMapping("/recommend")
    public List<Instrument> recommend(@RequestBody FinanceContext context) {
        return financeAdvisorService.recommend(context);
    }
}
