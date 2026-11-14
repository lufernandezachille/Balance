package com.balance.balance.service;

import com.balance.balance.dto.PlanRespuesta;
import com.balance.balance.model.Plan;
import com.balance.balance.repository.PlanRepository;
import org.springframework.stereotype.Service;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public PlanRespuesta obtenerPlanPremium() {

        Plan plan = planRepository.findByNombre("PREMIUM")
                .orElseThrow(() -> new RuntimeException("No se encontró el plan Premium"));

        return new PlanRespuesta(
                plan.getNombre(),
                plan.getPrecioMensual(),
                plan.getPrecioAnual()
        );
    }
}