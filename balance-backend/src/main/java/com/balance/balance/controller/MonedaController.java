package com.balance.balance.controller;

import com.balance.balance.model.Moneda;
import com.balance.balance.repository.MonedaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monedas")
public class MonedaController {

    private final MonedaRepository monedaRepository;

    public MonedaController(MonedaRepository monedaRepository) {
        this.monedaRepository = monedaRepository;
    }

    @GetMapping
    public List<Moneda> listarMonedas() {
        return monedaRepository.findAll();
    }
}