package com.balance.balance.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "suscripciones")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "frecuencia_pago", nullable = false)
    private FrecuenciaPago frecuenciaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSuscripcion estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_renovacion")
    private LocalDate fechaRenovacion;

    @Column(name = "id_externo_pasarela")
    private String idExternoPasarela;

    public Suscripcion() {
    }

    public enum FrecuenciaPago {
        MENSUAL,
        ANUAL
    }

    public enum EstadoSuscripcion {
        ACTIVA,
        CANCELADA,
        EN_MORA,
        VENCIDA
    }

    @PrePersist
    protected void alCrear() {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now();
        }

        if (estado == null) {
            estado = EstadoSuscripcion.ACTIVA;
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public FrecuenciaPago getFrecuenciaPago() {
        return frecuenciaPago;
    }

    public void setFrecuenciaPago(FrecuenciaPago frecuenciaPago) {
        this.frecuenciaPago = frecuenciaPago;
    }

    public EstadoSuscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSuscripcion estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaRenovacion() {
        return fechaRenovacion;
    }

    public void setFechaRenovacion(LocalDate fechaRenovacion) {
        this.fechaRenovacion = fechaRenovacion;
    }

    public String getIdExternoPasarela() {
        return idExternoPasarela;
    }

    public void setIdExternoPasarela(String idExternoPasarela) {
        this.idExternoPasarela = idExternoPasarela;
    }
}
