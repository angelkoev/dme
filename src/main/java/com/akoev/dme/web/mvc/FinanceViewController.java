package com.akoev.dme.web.mvc;

import com.akoev.dme.finance.FinanceAdvisorService;
import com.akoev.dme.finance.FinanceContext;
import com.akoev.dme.finance.RiskTolerance;
import com.akoev.dme.finance.Sector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class FinanceViewController {

    private final FinanceAdvisorService financeAdvisorService;

    @GetMapping("/finance")
    public String view(Model model) {
        model.addAttribute("instruments", financeAdvisorService.listInstruments());
        model.addAttribute("riskTolerances", RiskTolerance.values());
        model.addAttribute("sectors", Sector.values());
        return "finance";
    }

    @PostMapping("/finance/recommend")
    public String recommend(@RequestParam RiskTolerance riskTolerance,
                             @RequestParam(required = false) Set<Sector> excludedSectors,
                             Model model) {
        FinanceContext context = new FinanceContext(riskTolerance, excludedSectors);
        model.addAttribute("recommendations", financeAdvisorService.recommend(context));
        model.addAttribute("selectedRiskTolerance", riskTolerance);
        model.addAttribute("instruments", financeAdvisorService.listInstruments());
        model.addAttribute("riskTolerances", RiskTolerance.values());
        model.addAttribute("sectors", Sector.values());
        return "finance";
    }
}
