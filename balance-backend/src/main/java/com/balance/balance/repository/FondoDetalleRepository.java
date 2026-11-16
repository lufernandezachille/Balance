
package com.balance.balance.repository;

import com.balance.balance.model.FondoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FondoDetalleRepository
        extends JpaRepository<FondoDetalle, Long> {

    List<FondoDetalle> findByFondoId(Long fondoId);

    Optional<FondoDetalle> findByFondoIdAndCuentaId(
            Long fondoId, Long cuentaId
    );

    List<FondoDetalle> findByFondoUsuarioId(Long usuarioId);

    List<FondoDetalle> findByCuentaId(Long cuentaId);
}
