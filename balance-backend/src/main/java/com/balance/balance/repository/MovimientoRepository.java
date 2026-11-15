package com.balance.balance.repository;

import com.balance.balance.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByUsuarioId(Long usuarioId);

    Optional<Movimiento> findByIdAndUsuarioId(Long id, Long usuarioId);
}