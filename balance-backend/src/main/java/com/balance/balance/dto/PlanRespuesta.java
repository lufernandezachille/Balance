package com.balance.balance.dto;

import java.math.BigDecimal;

public class PlanRespuesta {

    private String nombre;
    private BigDecimal precioMensual;
    private BigDecimal precioAnual;

    public PlanRespuesta(String nombre, BigDecimal precioMensual, BigDecimal precioAnual) {
        this.nombre = nombre;
        this.precioMensual = precioMensual;
        this.precioAnual = precioAnual;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public BigDecimal getPrecioAnual() {
        return precioAnual;
    }
}