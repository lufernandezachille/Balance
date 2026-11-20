package com.balance.balance.service;

import com.balance.balance.model.Cuenta;
import com.balance.balance.model.Fondo;
import com.balance.balance.model.FondoDetalle;
import com.balance.balance.model.Movimiento;
import com.balance.balance.model.MovimientoRecurrente;
import com.balance.balance.repository.CuentaRepository;
import com.balance.balance.repository.FondoRepository;
import com.balance.balance.repository.FondoDetalleRepository;
import com.balance.balance.repository.MovimientoRecurrenteRepository;
import com.balance.balance.repository.MovimientoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final FondoRepository fondoRepository;
    private final FondoDetalleRepository fondoDetalleRepository;
    private final MovimientoRepository movimientoRepository;
    private final MovimientoRecurrenteRepository recurrenteRepository;

    public CuentaService(
            CuentaRepository cuentaRepository,
            FondoRepository fondoRepository,
            FondoDetalleRepository fondoDetalleRepository,
            MovimientoRepository movimientoRepository,
            MovimientoRecurrenteRepository recurrenteRepository) {

        this.cuentaRepository = cuentaRepository;
        this.fondoRepository = fondoRepository;
        this.fondoDetalleRepository = fondoDetalleRepository;
        this.movimientoRepository = movimientoRepository;
        this.recurrenteRepository = recurrenteRepository;
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

        // 1. Mantener los movimientos en el historial, desvinculando la cuenta eliminada
        List<Movimiento> movimientos = movimientoRepository.findByUsuarioId(usuarioId);
        for (Movimiento m : movimientos) {
            boolean modificado = false;
            if (m.getCuenta() != null && m.getCuenta().getId().equals(id)) {
                m.setCuenta(null);
                modificado = true;
            }
            if (m.getCuentaDestino() != null && m.getCuentaDestino().getId().equals(id)) {
                m.setCuentaDestino(null);
                modificado = true;
            }
            if (modificado) {
                movimientoRepository.save(m);
            }
        }

        // 2. Desvincular y desactivar los movimientos recurrentes asociados a esta cuenta
        List<MovimientoRecurrente> recurrentes = recurrenteRepository.findByUsuarioId(usuarioId);
        for (MovimientoRecurrente r : recurrentes) {
            if (r.getCuenta() != null && r.getCuenta().getId().equals(id)) {
                r.setCuenta(null);
                r.setActivo(false);
                recurrenteRepository.save(r);
            }
        }

        // 3. Eliminar los detalles de fondos de esta cuenta (esto descuenta el saldo de cada fondo/meta)
        List<FondoDetalle> detalles = fondoDetalleRepository.findByCuentaId(id);
        fondoDetalleRepository.deleteAll(detalles);

        // 4. Eliminar la cuenta (esto la descuenta automáticamente del patrimonio total)
        cuentaRepository.delete(cuenta);
    }
}