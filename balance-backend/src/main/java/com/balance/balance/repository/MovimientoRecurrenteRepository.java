package com.balance.balance.repository;

import com.balance.balance.model.MovimientoRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimientoRecurrenteRepository extends JpaRepository<MovimientoRecurrente, Long> {

    List<MovimientoRecurrente> findByUsuarioId(Long usuarioId);

    Optional<MovimientoRecurrente> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<MovimientoRecurrente> findByActivoTrue();
}
