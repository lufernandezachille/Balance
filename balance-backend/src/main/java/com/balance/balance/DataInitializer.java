package com.balance.balance;

import com.balance.balance.model.Moneda;
import com.balance.balance.model.Plan;
import com.balance.balance.repository.MonedaRepository;
import com.balance.balance.repository.PlanRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner inicializarDatos(
            MonedaRepository monedaRepository,
            PlanRepository planRepository) {

        return args -> {

            // MONEDAS

            crearMoneda(monedaRepository, "ARS", "Peso argentino", "$", 2);
            crearMoneda(monedaRepository, "USD", "Dólar estadounidense", "US$", 2);
            crearMoneda(monedaRepository, "EUR", "Euro", "€", 2);
            crearMoneda(monedaRepository, "MXN", "Peso mexicano", "$", 2);
            crearMoneda(monedaRepository, "CLP", "Peso chileno", "$", 0);
            crearMoneda(monedaRepository, "COP", "Peso colombiano", "$", 2);

            // PLANES

            crearPlan(planRepository, "GRATIS");
            crearPlan(planRepository, "PREMIUM");

        };
    }

    private void crearMoneda(
            MonedaRepository repositorio,
            String codigo,
            String nombre,
            String simbolo,
            Integer decimales) {

        if (!repositorio.existsByCodigo(codigo)) {

            Moneda moneda = new Moneda();

            moneda.setCodigo(codigo);
            moneda.setNombre(nombre);
            moneda.setSimbolo(simbolo);
            moneda.setDecimales(decimales);

            repositorio.save(moneda);
        }
    }

    private void crearPlan(
            PlanRepository repositorio,
            String nombre) {

        if (!repositorio.existsByNombre(nombre)) {

            Plan plan = new Plan();

            plan.setNombre(nombre);
            plan.setPrecioMensual(BigDecimal.ZERO);
            plan.setPrecioAnual(BigDecimal.ZERO);
            plan.setLimiteCuentas(null);
            plan.setLimiteFondos(null);
            plan.setPermiteMultimoneda(true);

            repositorio.save(plan);
        }
    }
}
