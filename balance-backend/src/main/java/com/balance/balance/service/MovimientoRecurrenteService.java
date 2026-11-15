
package com.balance.balance.service;

import com.balance.balance.model.*;
import com.balance.balance.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MovimientoRecurrenteService {

    private final MovimientoRecurrenteRepository recurrenteRepository;
    private final ExcepcionMovimientoRecurrenteRepository excepcionRepository;
    private final MovimientoService movimientoService;

    public MovimientoRecurrenteService(
            MovimientoRecurrenteRepository recurrenteRepository,
            ExcepcionMovimientoRecurrenteRepository excepcionRepository,
            MovimientoService movimientoService) {

        this.recurrenteRepository = recurrenteRepository;
        this.excepcionRepository = excepcionRepository;
        this.movimientoService = movimientoService;
    }

    // CREAR MOVIMIENTO RECURRENTE

    public MovimientoRecurrente crear(
            MovimientoRecurrente recurrente, Long usuarioId) {

        validar(recurrente);

        recurrente.setProximaFecha(recurrente.getFechaInicio());

        return recurrenteRepository.save(recurrente);
    }

    // LISTAR MOVIMIENTOS RECURRENTES

    public List<MovimientoRecurrente> listar(Long usuarioId) {
        return recurrenteRepository.findByUsuarioId(usuarioId);
    }

    // BUSCAR MOVIMIENTO RECURRENTE

    public MovimientoRecurrente buscar(Long id, Long usuarioId) {
        return recurrenteRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Movimiento recurrente no encontrado"));
    }

    // MODIFICAR MOVIMIENTO RECURRENTE

    public MovimientoRecurrente actualizar(
            Long id, MovimientoRecurrente datos, Long usuarioId) {

        MovimientoRecurrente recurrente = buscar(id, usuarioId);

        validar(datos);

        recurrente.setTipo(datos.getTipo());
        recurrente.setMonto(datos.getMonto());
        recurrente.setMoneda(datos.getMoneda());
        recurrente.setCategoria(datos.getCategoria());
        recurrente.setCuenta(datos.getCuenta());
        recurrente.setFondo(datos.getFondo());
        recurrente.setFrecuencia(datos.getFrecuencia());
        recurrente.setFechaInicio(datos.getFechaInicio());
        recurrente.setFechaFin(datos.getFechaFin());
        recurrente.setDescripcion(datos.getDescripcion());

        recurrente.setProximaFecha(datos.getFechaInicio());

        return recurrenteRepository.save(recurrente);
    }

    // ELIMINAR MOVIMIENTO RECURRENTE

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        MovimientoRecurrente recurrente = buscar(id, usuarioId);

        // Primero eliminamos las excepciones asociadas
        excepcionRepository.deleteAll(
                excepcionRepository.findByMovimientoRecurrenteId(id)
        );

        // Después eliminamos el movimiento recurrente
        recurrenteRepository.delete(recurrente);
    }

    // AGREGAR EXCEPCION

    public ExcepcionMovimientoRecurrente agregarExcepcion(
            Long id, LocalDate fecha, Long usuarioId) {

        MovimientoRecurrente recurrente = buscar(id, usuarioId);

        if (fecha == null) {
            throw new RuntimeException("La fecha de excepción es obligatoria");
        }

        if (fecha.isBefore(recurrente.getFechaInicio()) ||
                (recurrente.getFechaFin() != null &&
                        fecha.isAfter(recurrente.getFechaFin()))) {
            throw new RuntimeException(
                    "La fecha de excepción está fuera del período de recurrencia");
        }

        if (excepcionRepository.existsByMovimientoRecurrenteIdAndFecha(id, fecha)) {
            throw new RuntimeException("Ya existe una excepción para esa fecha");
        }

        ExcepcionMovimientoRecurrente excepcion =
                new ExcepcionMovimientoRecurrente();

        excepcion.setMovimientoRecurrente(recurrente);
        excepcion.setFecha(fecha);

        return excepcionRepository.save(excepcion);
    }

    // LISTAR EXCEPCIONES

    public List<ExcepcionMovimientoRecurrente> listarExcepciones(
            Long id, Long usuarioId) {

        buscar(id, usuarioId);

        return excepcionRepository.findByMovimientoRecurrenteId(id);
    }

    // ELIMINAR EXCEPCION

    @Transactional
    public void eliminarExcepcion(
            Long id, LocalDate fecha, Long usuarioId) {

        buscar(id, usuarioId);

        if (!excepcionRepository.existsByMovimientoRecurrenteIdAndFecha(id, fecha)) {
            throw new RuntimeException("Excepción no encontrada");
        }

        excepcionRepository.deleteByMovimientoRecurrenteIdAndFecha(id, fecha);
    }

    // PROCESAR MOVIMIENTOS QUE YA LLEGARON A SU FECHA

    @Transactional
    public void procesarVencidos() {

        LocalDate hoy = LocalDate.now();

        List<MovimientoRecurrente> recurrentes =
                recurrenteRepository.findByActivoTrue();

        for (MovimientoRecurrente recurrente : recurrentes) {

            LocalDate fecha = recurrente.getProximaFecha();

            while (fecha != null && !fecha.isAfter(hoy)) {

                // Si ya terminó la recurrencia, la desactivamos.
                if (recurrente.getFechaFin() != null &&
                        fecha.isAfter(recurrente.getFechaFin())) {

                    recurrente.setActivo(false);
                    break;
                }

                // Si la fecha no es una excepción, generamos el movimiento.
                boolean esExcepcion =
                        excepcionRepository
                                .existsByMovimientoRecurrenteIdAndFecha(
                                        recurrente.getId(), fecha);

                if (!esExcepcion) {

                    Movimiento movimiento = new Movimiento();

                    movimiento.setUsuario(recurrente.getUsuario());
                    movimiento.setTipo(recurrente.getTipo());
                    movimiento.setMonto(recurrente.getMonto());
                    movimiento.setMoneda(recurrente.getMoneda());
                    movimiento.setCategoria(recurrente.getCategoria());
                    movimiento.setCuenta(recurrente.getCuenta());
                    movimiento.setFondo(recurrente.getFondo());
                    movimiento.setFecha(fecha);
                    movimiento.setDescripcion(recurrente.getDescripcion());

                    movimientoService.crearMovimiento(
                            movimiento, recurrente.getUsuario().getId());
                }

                // Calculamos la siguiente fecha.
                fecha = calcularSiguienteFecha(
                        fecha, recurrente.getFrecuencia());

                recurrente.setProximaFecha(fecha);

                // Si ya superamos la fecha final, desactivamos.
                if (recurrente.getFechaFin() != null &&
                        fecha.isAfter(recurrente.getFechaFin())) {

                    recurrente.setActivo(false);
                    break;
                }
            }

            recurrenteRepository.save(recurrente);
        }
    }

    // CALCULAR SIGUIENTE FECHA

    private LocalDate calcularSiguienteFecha(
            LocalDate fecha, MovimientoRecurrente.Frecuencia frecuencia) {

        switch (frecuencia) {
            case DIARIA:
                return fecha.plusDays(1);

            case SEMANAL:
                return fecha.plusWeeks(1);

            case MENSUAL:
                return fecha.plusMonths(1);

            default:
                throw new RuntimeException("Frecuencia no válida");
        }
    }

    // VALIDACIONES BÁSICAS

    private void validar(MovimientoRecurrente recurrente) {

        if (recurrente.getTipo() == null ||
                recurrente.getTipo() == Movimiento.TipoMovimiento.TRANSFERENCIA) {
            throw new RuntimeException(
                    "Los movimientos recurrentes solo pueden ser ingresos o gastos");
        }

        if (recurrente.getMonto() == null ||
                recurrente.getMonto().signum() <= 0) {
            throw new RuntimeException("El monto debe ser mayor a cero");
        }

        if (recurrente.getMoneda() == null ||
                recurrente.getMoneda().getId() == null) {
            throw new RuntimeException("La moneda es obligatoria");
        }

        if (recurrente.getFrecuencia() == null) {
            throw new RuntimeException("La frecuencia es obligatoria");
        }

        if (recurrente.getFechaInicio() == null) {
            throw new RuntimeException("La fecha de inicio es obligatoria");
        }

        if (recurrente.getFechaFin() != null &&
                recurrente.getFechaFin().isBefore(recurrente.getFechaInicio())) {
            throw new RuntimeException(
                    "La fecha final no puede ser anterior a la fecha de inicio");
        }
    }
}
