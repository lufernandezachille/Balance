package com.balance.balance.repository;

import com.balance.balance.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Long> {

    Optional<Sesion> findByTokenHash(String tokenHash);

    List<Sesion> findByUsuarioId(Long usuarioId);

    void deleteByTokenHash(String tokenHash);
}
