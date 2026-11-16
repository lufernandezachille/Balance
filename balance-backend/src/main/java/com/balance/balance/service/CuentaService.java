
package com.balance.balance.service;

import com.balance.balance.model.Cuenta;
import com.balance.balance.model.Fondo;
import com.balance.balance.model.FondoDetalle;
import com.balance.balance.repository.CuentaRepository;
import com.balance.balance.repository.FondoRepository;
import com.balance.balance.repository.FondoDetalleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final FondoRepository fondoRepository;
    private final FondoDetalleRepository fondoDetalleRepository;

    public CuentaService(
            CuentaRepository cuentaRepository,
            FondoRepository fondoRepository,
            FondoDetalleRepository fondoDetalleRepository) {

        this.cuentaRepository = cuentaRepository;
        this.fondoRepository = fondoRepository;
        this.fondoDetalleRepository = fondoDetalleRepository;
    }

    @Transactional
    public Cuenta crearCuenta(Cuenta cuenta) {

        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);

        Fondo fondoGeneral = fondoRepository
                .findByUsuarioIdAndEsFondoGeneralTrue(
                        cuenta.getUsuario().getId())
                .orElseThrow(() ->
                        new RuntimeException("No existe el fondo general"));

        FondoDetalle detalle = new FondoDetalle();
        detalle.setFondo(fondoGeneral);
        detalle.setCuenta(cuentaGuardada);

        BigDecimal saldoInicial = cuentaGuardada.getSaldoActual();

        if (saldoInicial == null) {
            saldoInicial = BigDecimal.ZERO;
        }

        detalle.setSaldoAsignado(saldoInicial);

        fondoDetalleRepository.save(detalle);

        return cuentaGuardada;
    }

    public List<Cuenta> listarCuentas(Long usuarioId) {
        return cuentaRepository.findByUsuarioId(usuarioId);
    }

    public Cuenta buscarCuenta(Long id, Long usuarioId) {
        return cuentaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("La cuenta no existe"));
    }

    @Transactional
    public Cuenta actualizarCuenta(
            Long id, Long usuarioId, Cuenta datos) {

        Cuenta cuenta = buscarCuenta(id, usuarioId);

        if (datos.getMoneda() == null ||
                datos.getMoneda().getId() == null) {
            throw new RuntimeException("La moneda es obligatoria");
        }

        if (!cuenta.getMoneda().getId()
                .equals(datos.getMoneda().getId())
                && cuenta.getSaldoActual().compareTo(BigDecimal.ZERO) != 0) {

            throw new RuntimeException(
                    "No se puede cambiar la moneda de una cuenta con saldo");
        }

        cuenta.setNombre(datos.getNombre());
        cuenta.setTipo(datos.getTipo());
        cuenta.setMoneda(datos.getMoneda());
        cuenta.setIncluirEnPatrimonio(datos.isIncluirEnPatrimonio());

        return cuentaRepository.save(cuenta);
    }

    @Transactional
    public void eliminarCuenta(Long id, Long usuarioId) {

        Cuenta cuenta = buscarCuenta(id, usuarioId);

        if (cuenta.getSaldoActual().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException(
                    "No se puede eliminar una cuenta que tiene saldo");
        }

        List<FondoDetalle> detalles =
                fondoDetalleRepository.findByCuentaId(id);

        fondoDetalleRepository.deleteAll(detalles);

        cuentaRepository.delete(cuenta);
    }
}
