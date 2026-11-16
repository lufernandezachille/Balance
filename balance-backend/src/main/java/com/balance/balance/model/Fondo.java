
package com.balance.balance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fondos")
public class Fondo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "es_fondo_general", nullable = false)
    private boolean esFondoGeneral = false;

    @Column(name = "es_meta", nullable = false)
    private boolean esMeta = false;

    @Column(name = "monto_objetivo", precision = 15, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "fecha_objetivo")
    private LocalDate fechaObjetivo;

    @Column(name = "color_icono")
    private String colorIcono;

    public Fondo() {}

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsFondoGeneral() {
        return esFondoGeneral;
    }

    public void setEsFondoGeneral(boolean esFondoGeneral) {
        this.esFondoGeneral = esFondoGeneral;
    }

    public boolean isEsMeta() {
        return esMeta;
    }

    public void setEsMeta(boolean esMeta) {
        this.esMeta = esMeta;
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
