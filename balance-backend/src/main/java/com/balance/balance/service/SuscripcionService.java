package com.balance.balance.service;

import com.balance.balance.dto.SuscripcionRespuesta;
import com.balance.balance.model.Plan;
import com.balance.balance.model.Suscripcion;
import com.balance.balance.model.Usuario;
import com.balance.balance.repository.PlanRepository;
import com.balance.balance.repository.SuscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;

    public SuscripcionService(
            SuscripcionRepository suscripcionRepository,
            PlanRepository planRepository) {

        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
    }

    public SuscripcionRespuesta obtenerSuscripcionActual(Long usuarioId) {

        Suscripcion suscripcion = suscripcionRepository
                .findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                        usuarioId,
                        Suscripcion.EstadoSuscripcion.ACTIVA
                )
                .orElseThrow(() -> new RuntimeException(
                        "El usuario no tiene una suscripción activa"
                ));

        return new SuscripcionRespuesta(
                suscripcion.getPlan().getNombre(),
                suscripcion.getEstado().name(),
                suscripcion.getFrecuenciaPago().name(),
                suscripcion.getFechaInicio(),
                suscripcion.getFechaRenovacion()
        );
    }

    @Transactional
    public SuscripcionRespuesta activarPremium(
            Usuario usuario,
            String frecuencia) {

        // Validar la frecuencia elegida

        Suscripcion.FrecuenciaPago frecuenciaPago;

        try {
            frecuenciaPago = Suscripcion.FrecuenciaPago.valueOf(frecuencia);
        } catch (Exception e) {
            throw new RuntimeException("La frecuencia de pago no es válida");
        }

        // Buscar el plan Premium

        Plan plan = planRepository.findByNombre("PREMIUM")
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el plan Premium"
                ));

        // Cancelar la suscripción activa anterior

        suscripcionRepository
                .findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                        usuario.getId(),
                        Suscripcion.EstadoSuscripcion.ACTIVA
                )
                .ifPresent(suscripcion -> {
                    suscripcion.setEstado(
                            Suscripcion.EstadoSuscripcion.CANCELADA
                    );
                    suscripcionRepository.save(suscripcion);
                });

        // Crear la nueva suscripción

        LocalDate fechaInicio = LocalDate.now();

        Suscripcion nuevaSuscripcion = new Suscripcion();

        nuevaSuscripcion.setUsuario(usuario);
        nuevaSuscripcion.setPlan(plan);
        nuevaSuscripcion.setFrecuenciaPago(frecuenciaPago);
        nuevaSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        nuevaSuscripcion.setFechaInicio(fechaInicio);

        if (frecuenciaPago == Suscripcion.FrecuenciaPago.MENSUAL) {
            nuevaSuscripcion.setFechaRenovacion(fechaInicio.plusMonths(1));
        } else {
            nuevaSuscripcion.setFechaRenovacion(fechaInicio.plusYears(1));
        }

        suscripcionRepository.save(nuevaSuscripcion);

        return new SuscripcionRespuesta(
                plan.getNombre(),
                nuevaSuscripcion.getEstado().name(),
                nuevaSuscripcion.getFrecuenciaPago().name(),
                nuevaSuscripcion.getFechaInicio(),
                nuevaSuscripcion.getFechaRenovacion()
        );
    }
}