package com.balance.balance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCuenta tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moneda_id", nullable = false)
    private Moneda moneda;

    @Column(name = "saldo_actual", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "incluir_en_patrimonio", nullable = false)
    private boolean incluirEnPatrimonio = true;

    public Cuenta() {
    }

    public enum TipoCuenta {
        EFECTIVO,
        BANCO,
        BILLETERA_VIRTUAL,
        TARJETA_CREDITO,
        INVERSIONES
    }

    // Getters y setters

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

    public TipoCuenta getTipo() {
        return tipo;
    }

    public void setTipo(TipoCuenta tipo) {
        this.tipo = tipo;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
    }

    public boolean isIncluirEnPatrimonio() {
        return incluirEnPatrimonio;
    }

    public void setIncluirEnPatrimonio(boolean incluirEnPatrimonio) {
        this.incluirEnPatrimonio = incluirEnPatrimonio;
    }
}
