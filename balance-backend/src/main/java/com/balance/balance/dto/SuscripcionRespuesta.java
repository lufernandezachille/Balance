package com.balance.balance.dto;

import java.time.LocalDate;

public class SuscripcionRespuesta {

    private String nombrePlan;
    private String estado;
    private String frecuenciaPago;
    private LocalDate fechaInicio;
    private LocalDate fechaRenovacion;

    public SuscripcionRespuesta(String nombrePlan, String estado,
                                String frecuenciaPago, LocalDate fechaInicio,
                                LocalDate fechaRenovacion) {
        this.nombrePlan = nombrePlan;
        this.estado = estado;
        this.frecuenciaPago = frecuenciaPago;
        this.fechaInicio = fechaInicio;
        this.fechaRenovacion = fechaRenovacion;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public String getEstado() {
        return estado;
    }

    public String getFrecuenciaPago() {
        return frecuenciaPago;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaRenovacion() {
        return fechaRenovacion;
    }
}