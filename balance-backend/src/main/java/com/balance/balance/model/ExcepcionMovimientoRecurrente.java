
package com.balance.balance.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "excepciones_movimientos_recurrentes")
public class ExcepcionMovimientoRecurrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_recurrente_id", nullable = false)
    private MovimientoRecurrente movimientoRecurrente;

    @Column(nullable = false)
    private LocalDate fecha;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovimientoRecurrente getMovimientoRecurrente() {
        return movimientoRecurrente;
    }

    public void setMovimientoRecurrente(MovimientoRecurrente movimientoRecurrente) {
        this.movimientoRecurrente = movimientoRecurrente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
