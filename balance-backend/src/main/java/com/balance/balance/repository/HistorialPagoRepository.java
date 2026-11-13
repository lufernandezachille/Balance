package com.balance.balance.repository;

import com.balance.balance.model.HistorialPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialPagoRepository extends JpaRepository<HistorialPago, Long> {

    List<HistorialPago> findBySuscripcionId(Long suscripcionId);

    List<HistorialPago> findBySuscripcionIdOrderByFechaPagoDesc(Long suscripcionId);
}
