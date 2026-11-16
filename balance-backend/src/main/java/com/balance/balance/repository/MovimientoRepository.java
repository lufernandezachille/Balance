
package com.balance.balance.repository;

import com.balance.balance.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByUsuarioId(Long usuarioId);

    Optional<Movimiento> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
        SELECT m FROM Movimiento m
        WHERE m.usuario.id = :usuarioId
        AND (:tipo IS NULL OR m.tipo = :tipo)
        AND (:categoriaId IS NULL OR m.categoria.id = :categoriaId)
        AND (:cuentaId IS NULL OR m.cuenta.id = :cuentaId OR m.cuentaDestino.id = :cuentaId)
        AND (:fondoId IS NULL OR m.fondo.id = :fondoId OR m.fondoDestino.id = :fondoId)
        AND (:medioPago IS NULL OR m.medioPago = :medioPago)
        AND (:monedaId IS NULL OR m.moneda.id = :monedaId)
        AND (:fechaDesde IS NULL OR m.fecha >= :fechaDesde)
        AND (:fechaHasta IS NULL OR m.fecha <= :fechaHasta)
        AND (:montoMinimo IS NULL OR m.monto >= :montoMinimo)
        AND (:montoMaximo IS NULL OR m.monto <= :montoMaximo)
        ORDER BY m.fecha DESC
    """)
    List<Movimiento> filtrarMovimientos(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") Movimiento.TipoMovimiento tipo,
            @Param("categoriaId") Long categoriaId,
            @Param("cuentaId") Long cuentaId,
            @Param("fondoId") Long fondoId,
            @Param("medioPago") Movimiento.MedioPago medioPago,
            @Param("monedaId") Long monedaId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("montoMinimo") BigDecimal montoMinimo,
            @Param("montoMaximo") BigDecimal montoMaximo
    );
}
