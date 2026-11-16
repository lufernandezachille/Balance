package com.balance.balance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "fondos_detalles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"fondo_id", "cuenta_id"})
        }
)
public class FondoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fondo_id", nullable = false)
    private Fondo fondo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Column(name = "saldo_asignado", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal saldoAsignado = BigDecimal.ZERO;

    public FondoDetalle() {}

    public Long getId() {
        return id;
    }

    public Fondo getFondo() {
        return fondo;
    }

    public void setFondo(Fondo fondo) {
        this.fondo = fondo;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public BigDecimal getSaldoAsignado() {
        return saldoAsignado;
    }

    public void setSaldoAsignado(BigDecimal saldoAsignado) {
        this.saldoAsignado = saldoAsignado;
    }
}
