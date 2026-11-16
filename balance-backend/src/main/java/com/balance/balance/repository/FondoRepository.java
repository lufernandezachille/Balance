
package com.balance.balance.repository;

import com.balance.balance.model.Fondo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FondoRepository extends JpaRepository<Fondo, Long> {

    List<Fondo> findByUsuarioId(Long usuarioId);

    Optional<Fondo> findByIdAndUsuarioId(Long id, Long usuarioId);

    Optional<Fondo> findByUsuarioIdAndEsFondoGeneralTrue(Long usuarioId);

    List<Fondo> findByUsuarioIdAndEsMetaTrue(Long usuarioId);

    Optional<Fondo> findByIdAndUsuarioIdAndEsMetaTrue(Long id, Long usuarioId);
}
