package com.balance.balance.repository;

import com.balance.balance.model.Moneda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MonedaRepository extends JpaRepository<Moneda, Long> {

    Optional<Moneda> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
