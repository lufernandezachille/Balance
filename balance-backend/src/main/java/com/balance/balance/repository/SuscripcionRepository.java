package com.balance.balance.repository;

import com.balance.balance.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    List<Suscripcion> findByUsuarioId(Long usuarioId);

    Optional<Suscripcion> findByUsuarioIdAndEstado(
            Long usuarioId,
            Suscripcion.EstadoSuscripcion estado
    );

    Optional<Suscripcion> findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
            Long usuarioId,
            Suscripcion.EstadoSuscripcion estado
    );
}