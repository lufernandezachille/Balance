package com.balance.balance.controller;

import com.balance.balance.dto.CotizacionRespuesta;
import com.balance.balance.service.CotizacionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @GetMapping
    public CotizacionRespuesta obtenerCotizaciones() {
        return cotizacionService.obtenerCotizaciones();
    }

    @GetMapping("/convertir")
    public BigDecimal convertir(
            @RequestParam BigDecimal monto,
            @RequestParam String origen,
            @RequestParam String destino) {

        return cotizacionService.convertir(monto, origen, destino);
    }
}
