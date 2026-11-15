package com.balance.balance.repository;

import com.balance.balance.model.ExcepcionMovimientoRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExcepcionMovimientoRecurrenteRepository extends JpaRepository<ExcepcionMovimientoRecurrente, Long> {

    List<ExcepcionMovimientoRecurrente> findByMovimientoRecurrenteId(Long movimientoRecurrenteId);

    boolean existsByMovimientoRecurrenteIdAndFecha(Long movimientoRecurrenteId, LocalDate fecha);

    void deleteByMovimientoRecurrenteIdAndFecha(Long movimientoRecurrenteId, LocalDate fecha);
}

