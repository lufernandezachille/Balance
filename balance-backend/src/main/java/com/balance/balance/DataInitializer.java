package com.balance.balance;

import com.balance.balance.model.Categoria;
import com.balance.balance.model.Moneda;
import com.balance.balance.model.Plan;
import com.balance.balance.repository.CategoriaRepository;
import com.balance.balance.repository.MonedaRepository;
import com.balance.balance.repository.PlanRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner inicializarDatos(
            MonedaRepository monedaRepository,
            PlanRepository planRepository,
            CategoriaRepository categoriaRepository) {

        return args -> {

            // MONEDAS
            crearMoneda(monedaRepository, "ARS", "Peso argentino", "$", 2);
            crearMoneda(monedaRepository, "USD", "Dólar estadounidense", "US$", 2);
            crearMoneda(monedaRepository, "EUR", "Euro", "€", 2);
            crearMoneda(monedaRepository, "MXN", "Peso mexicano", "$", 2);
            crearMoneda(monedaRepository, "CLP", "Peso chileno", "$", 0);
            crearMoneda(monedaRepository, "COP", "Peso colombiano", "$", 2);

            // PLANES
            crearPlan(planRepository, "GRATIS",
                    BigDecimal.ZERO, BigDecimal.ZERO);

            crearPlan(planRepository, "PREMIUM",
                    new BigDecimal("4999"), new BigDecimal("36000"));

            // CATEGORÍAS DE GASTOS
            List<String> gastos = List.of(
                    "Vivienda", "Alimentación", "Transporte", "Ropa",
                    "Salud", "Educación", "Entretenimiento", "Viajes", "Otros"
            );

            for (String nombre : gastos) {
                crearCategoria(categoriaRepository, nombre, Categoria.TipoCategoria.GASTO);
            }

            // CATEGORÍAS DE INGRESOS
            List<String> ingresos = List.of(
                    "Sueldo y trabajo", "Negocios y ventas", "Regalos",
                    "Inversiones", "Ayudas y prestaciones", "Reembolsos", "Otros"
            );

            for (String nombre : ingresos) {
                crearCategoria(categoriaRepository, nombre, Categoria.TipoCategoria.INGRESO);
            }
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
            String nombre,
            BigDecimal precioMensual,
            BigDecimal precioAnual) {

        if (!repositorio.existsByNombre(nombre)) {
            Plan plan = new Plan();
            plan.setNombre(nombre);
            plan.setPrecioMensual(precioMensual);
            plan.setPrecioAnual(precioAnual);
            plan.setLimiteCuentas(null);
            plan.setLimiteFondos(null);
            plan.setPermiteMultimoneda(true);
            repositorio.save(plan);
        }
    }

    private void crearCategoria(
            CategoriaRepository repositorio,
            String nombre,
            Categoria.TipoCategoria tipo) {

        boolean yaExiste = repositorio.findByUsuarioIdOrUsuarioIsNull(null)
                .stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre) && c.getTipo() == tipo);

        if (!yaExiste) {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoria.setTipo(tipo);
            categoria.setUsuario(null); // Global para todos los usuarios
            repositorio.save(categoria);
        }
    }
}