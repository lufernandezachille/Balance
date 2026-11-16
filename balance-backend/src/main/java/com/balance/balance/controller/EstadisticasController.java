
package com.balance.balance.controller;

import com.balance.balance.dto.estadisticas.EstadisticasRespuesta;
import com.balance.balance.service.EstadisticasService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    public EstadisticasController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping
    public EstadisticasRespuesta obtenerEstadisticas(
            Authentication authentication,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {

        String correo = authentication.getName();

        return estadisticasService.obtenerEstadisticas(
                correo, mes, anio
        );
    }
}
