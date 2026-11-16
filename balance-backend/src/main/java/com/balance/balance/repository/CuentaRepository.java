
package com.balance.balance.repository;

import com.balance.balance.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByUsuarioId(Long usuarioId);

    Optional<Cuenta> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Cuenta> findByUsuarioIdAndIncluirEnPatrimonioTrue(Long usuarioId);
}
