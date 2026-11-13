package com.balance.balance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "planes")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "precio_mensual", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "precio_anual", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioAnual;

    @Column(name = "limite_cuentas")
    private Integer limiteCuentas;

    @Column(name = "limite_fondos")
    private Integer limiteFondos;

    @Column(name = "permite_multimoneda", nullable = false)
    private Boolean permiteMultimoneda;

    public Plan() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public void setPrecioMensual(BigDecimal precioMensual) {
        this.precioMensual = precioMensual;
    }

    public BigDecimal getPrecioAnual() {
        return precioAnual;
    }

    public void setPrecioAnual(BigDecimal precioAnual) {
        this.precioAnual = precioAnual;
    }

    public Integer getLimiteCuentas() {
        return limiteCuentas;
    }

    public void setLimiteCuentas(Integer limiteCuentas) {
        this.limiteCuentas = limiteCuentas;
    }

    public Integer getLimiteFondos() {
        return limiteFondos;
    }

    public void setLimiteFondos(Integer limiteFondos) {
        this.limiteFondos = limiteFondos;
    }

    public Boolean getPermiteMultimoneda() {
        return permiteMultimoneda;
    }

    public void setPermiteMultimoneda(Boolean permiteMultimoneda) {
        this.permiteMultimoneda = permiteMultimoneda;
    }
}
