package com.balance.balance.controller;

import com.balance.balance.dto.PlanRespuesta;
import com.balance.balance.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/premium")
    public PlanRespuesta obtenerPlanPremium() {
        return planService.obtenerPlanPremium();
    }
}