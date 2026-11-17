
package com.balance.balance.service;

import com.balance.balance.model.*;
import com.balance.balance.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final FondoRepository fondoRepository;
    private final FondoDetalleRepository fondoDetalleRepository;
    private final CategoriaRepository categoriaRepository;
    private final MonedaRepository monedaRepository;

    public MovimientoService(
            MovimientoRepository movimientoRepository,
            CuentaRepository cuentaRepository,
            FondoRepository fondoRepository,
            FondoDetalleRepository fondoDetalleRepository,
            CategoriaRepository categoriaRepository,
            MonedaRepository monedaRepository) {

        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
        this.fondoRepository = fondoRepository;
        this.fondoDetalleRepository = fondoDetalleRepository;
        this.categoriaRepository = categoriaRepository;
        this.monedaRepository = monedaRepository;
    }

    @Transactional
    public Movimiento crearMovimiento(Movimiento movimiento, Long usuarioId) {
        validarRelaciones(movimiento, usuarioId);
        aplicarImpacto(movimiento, true);

        return movimientoRepository.save(movimiento);
    }

    public List<Movimiento> listarMovimientos(Long usuarioId) {
        return movimientoRepository.findByUsuarioId(usuarioId);
    }

    public Movimiento buscarMovimiento(Long id, Long usuarioId) {
        return movimientoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
    }

    @Transactional
    public Movimiento actualizarMovimiento(Long id, Movimiento datos, Long usuarioId) {

        Movimiento movimiento = buscarMovimiento(id, usuarioId);

        // Revertimos el efecto del movimiento anterior.
        aplicarImpacto(movimiento, false);

        // Validamos los datos nuevos.
        validarRelaciones(datos, usuarioId);

        movimiento.setTipo(datos.getTipo());
        movimiento.setMonto(datos.getMonto());
        movimiento.setMoneda(datos.getMoneda());
        movimiento.setCategoria(datos.getCategoria());
        movimiento.setCuenta(datos.getCuenta());
        movimiento.setCuentaDestino(datos.getCuentaDestino());
        movimiento.setFondo(datos.getFondo());
        movimiento.setFondoDestino(datos.getFondoDestino());
        movimiento.setMedioPago(datos.getMedioPago());
        movimiento.setFecha(datos.getFecha());
        movimiento.setDescripcion(datos.getDescripcion());

        // Aplicamos el nuevo efecto.
        aplicarImpacto(movimiento, true);

        return movimientoRepository.save(movimiento);
    }

    @Transactional
    public void eliminarMovimiento(Long id, Long usuarioId) {

        Movimiento movimiento = buscarMovimiento(id, usuarioId);

        // Revertimos el efecto antes de eliminarlo.
        aplicarImpacto(movimiento, false);

        movimientoRepository.delete(movimiento);
    }

    private void aplicarImpacto(Movimiento movimiento, boolean aplicar) {

        BigDecimal monto = movimiento.getMonto();

        if (!aplicar) {
            monto = monto.negate();
        }

        switch (movimiento.getTipo()) {

            case INGRESO:
                actualizarCuenta(movimiento.getCuenta(), monto);
                actualizarFondo(
                        movimiento.getFondo(),
                        movimiento.getCuenta(),
                        monto
                );
                break;

            case GASTO:
                actualizarCuenta(movimiento.getCuenta(), monto.negate());
                actualizarFondo(
                        movimiento.getFondo(),
                        movimiento.getCuenta(),
                        monto.negate()
                );
                break;

            case TRANSFERENCIA:
                actualizarCuenta(movimiento.getCuenta(), monto.negate());
                actualizarFondo(
                        movimiento.getFondo(),
                        movimiento.getCuenta(),
                        monto.negate()
                );

                actualizarCuenta(movimiento.getCuentaDestino(), monto);
                actualizarFondo(
                        movimiento.getFondoDestino(),
                        movimiento.getCuentaDestino(),
                        monto
                );
                break;
        }
    }

    private void actualizarCuenta(Cuenta cuenta, BigDecimal monto) {

        if (cuenta != null) {
            cuenta.setSaldoActual(cuenta.getSaldoActual().add(monto));
            cuentaRepository.save(cuenta);
        }
    }

    private void actualizarFondo(Fondo fondo, Cuenta cuenta, BigDecimal monto) {

        if (fondo == null) {
            fondo = fondoRepository
                    .findByUsuarioIdAndEsFondoGeneralTrue(cuenta.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("No se encontró el fondo General"));
        }

        FondoDetalle detalle = fondoDetalleRepository
                .findByFondoIdAndCuentaId(fondo.getId(), cuenta.getId())
                .orElse(null);

        if (detalle == null) {
            detalle = new FondoDetalle();
            detalle.setFondo(fondo);
            detalle.setCuenta(cuenta);
            detalle.setSaldoAsignado(BigDecimal.ZERO);
        }

        BigDecimal nuevoSaldo = detalle.getSaldoAsignado().add(monto);

        if (nuevoSaldo.signum() < 0) {
            throw new RuntimeException("El fondo no tiene suficiente dinero en esta cuenta");
        }

        detalle.setSaldoAsignado(nuevoSaldo);
        fondoDetalleRepository.save(detalle);
    }

    private void validarRelaciones(Movimiento movimiento, Long usuarioId) {

        if (movimiento.getMonto() == null ||
                movimiento.getMonto().signum() <= 0) {
            throw new RuntimeException("El monto debe ser mayor a cero");
        }

        if (movimiento.getTipo() == null || movimiento.getFecha() == null) {
            throw new RuntimeException("El tipo y la fecha son obligatorios");
        }

        if (movimiento.getMoneda() == null ||
                movimiento.getMoneda().getId() == null ||
                !monedaRepository.existsById(movimiento.getMoneda().getId())) {
            throw new RuntimeException("La moneda no existe");
        }

        if (movimiento.getCuenta() != null) {
            movimiento.setCuenta(
                    cuentaRepository.findByIdAndUsuarioId(
                                    movimiento.getCuenta().getId(), usuarioId)
                            .orElseThrow(() -> new RuntimeException("Cuenta no válida"))
            );
        }

        if (movimiento.getCuentaDestino() != null) {
            movimiento.setCuentaDestino(
                    cuentaRepository.findByIdAndUsuarioId(
                                    movimiento.getCuentaDestino().getId(), usuarioId)
                            .orElseThrow(() -> new RuntimeException("Cuenta destino no válida"))
            );
        }

        if (movimiento.getFondo() != null) {
            movimiento.setFondo(
                    fondoRepository.findByIdAndUsuarioId(
                                    movimiento.getFondo().getId(), usuarioId)
                            .orElseThrow(() -> new RuntimeException("Fondo no válido"))
            );
        }

        if (movimiento.getFondoDestino() != null) {
            movimiento.setFondoDestino(
                    fondoRepository.findByIdAndUsuarioId(
                                    movimiento.getFondoDestino().getId(), usuarioId)
                            .orElseThrow(() -> new RuntimeException("Fondo destino no válido"))
            );
        }

        if (movimiento.getCategoria() != null) {

            Categoria categoria = categoriaRepository.findById(
                            movimiento.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            if (categoria.getUsuario() != null &&
                    !categoria.getUsuario().getId().equals(usuarioId)) {
                throw new RuntimeException("La categoría no pertenece al usuario");
            }

            movimiento.setCategoria(categoria);
        }

        // Los ingresos y gastos necesitan una cuenta de origen.
        if (movimiento.getTipo() != Movimiento.TipoMovimiento.TRANSFERENCIA
                && movimiento.getCuenta() == null) {
            throw new RuntimeException("El movimiento necesita una cuenta");
        }

        // Las transferencias necesitan una cuenta de origen y otra de destino.
        if (movimiento.getTipo() == Movimiento.TipoMovimiento.TRANSFERENCIA) {

            if (movimiento.getCuenta() == null ||
                    movimiento.getCuentaDestino() == null) {
                throw new RuntimeException("La transferencia necesita una cuenta de origen y otra de destino");
            }

            Long fondoOrigenId = movimiento.getFondo() != null ? movimiento.getFondo().getId() : null;
            Long fondoDestinoId = movimiento.getFondoDestino() != null ? movimiento.getFondoDestino().getId() : null;

            if (movimiento.getCuenta().getId().equals(movimiento.getCuentaDestino().getId())
                    && java.util.Objects.equals(fondoOrigenId, fondoDestinoId)) {
                throw new RuntimeException("La cuenta y el fondo de origen y destino no pueden ser exactamente iguales");
            }

            if (!movimiento.getCuenta().getMoneda().getId().equals(
                    movimiento.getMoneda().getId()) ||
                    !movimiento.getCuentaDestino().getMoneda().getId().equals(
                            movimiento.getMoneda().getId())) {
                throw new RuntimeException("Las cuentas deben utilizar la moneda del movimiento");
            }
        } else if (!movimiento.getCuenta().getMoneda().getId().equals(
                movimiento.getMoneda().getId())) {
            throw new RuntimeException("La moneda del movimiento debe coincidir con la de la cuenta");
        }
    }

    public List<Movimiento> filtrarMovimientos(
            Long usuarioId,
            Movimiento.TipoMovimiento tipo,
            Long categoriaId,
            Long cuentaId,
            Long fondoId,
            Movimiento.MedioPago medioPago,
            Long monedaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            BigDecimal montoMinimo,
            BigDecimal montoMaximo) {

        if (fechaDesde != null && fechaHasta != null &&
                fechaDesde.isAfter(fechaHasta)) {
            throw new RuntimeException("La fecha desde no puede ser posterior a la fecha hasta");
        }

        if (montoMinimo != null && montoMaximo != null &&
                montoMinimo.compareTo(montoMaximo) > 0) {
            throw new RuntimeException("El monto mínimo no puede ser mayor al máximo");
        }

        return movimientoRepository.filtrarMovimientos(
                usuarioId,
                tipo,
                categoriaId,
                cuentaId,
                fondoId,
                medioPago,
                monedaId,
                fechaDesde,
                fechaHasta,
                montoMinimo,
                montoMaximo
        );
    }

}
