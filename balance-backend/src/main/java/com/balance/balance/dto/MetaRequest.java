
package com.balance.balance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MetaRequest {

    private String nombre;
    private BigDecimal montoObjetivo;
    private LocalDate fechaObjetivo;
    private String colorIcono;

    public MetaRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMontoObjetivo() {
        return montoObjetivo;
    }

    public void setMontoObjetivo(BigDecimal montoObjetivo) {
        this.montoObjetivo = montoObjetivo;
    }

    public LocalDate getFechaObjetivo() {
        return fechaObjetivo;
    }

    public void setFechaObjetivo(LocalDate fechaObjetivo) {
        this.fechaObjetivo = fechaObjetivo;
    }

    public String getColorIcono() {
        return colorIcono;
    }

    public void setColorIcono(String colorIcono) {
        this.colorIcono = colorIcono;
    }
}
