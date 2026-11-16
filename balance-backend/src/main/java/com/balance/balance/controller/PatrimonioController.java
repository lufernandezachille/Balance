package com.balance.balance.controller;

import com.balance.balance.dto.PatrimonioRespuesta;
import com.balance.balance.service.PatrimonioService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patrimonio")
public class PatrimonioController {

    private final PatrimonioService patrimonioService;

    public PatrimonioController(PatrimonioService patrimonioService) {
        this.patrimonioService = patrimonioService;
    }

    @GetMapping
    public PatrimonioRespuesta obtenerPatrimonio(Authentication authentication) {

        String correo = authentication.getName();

        return patrimonioService.calcularPatrimonio(correo);
    }
}
