package com.balance.balance.service;

import com.balance.balance.dto.SuscripcionRespuesta;
import com.balance.balance.model.Plan;
import com.balance.balance.model.Suscripcion;
import com.balance.balance.model.Usuario;
import com.balance.balance.repository.PlanRepository;
import com.balance.balance.repository.SuscripcionRepository;
import com.balance.balance.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;
    private final UsuarioRepository usuarioRepository;

    public SuscripcionService(
            SuscripcionRepository suscripcionRepository,
            PlanRepository planRepository,
            UsuarioRepository usuarioRepository) {

        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public SuscripcionRespuesta obtenerSuscripcionActual(Long usuarioId) {

        return suscripcionRepository
                .findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                        usuarioId,
                        Suscripcion.EstadoSuscripcion.ACTIVA
                )
                .map(suscripcion -> new SuscripcionRespuesta(
                        suscripcion.getPlan().getNombre(),
                        suscripcion.getEstado().name(),
                        suscripcion.getFrecuenciaPago().name(),
                        suscripcion.getFechaInicio(),
                        suscripcion.getFechaRenovacion()
                ))
                .orElseGet(() -> new SuscripcionRespuesta(
                        "GRATIS",
                        Suscripcion.EstadoSuscripcion.ACTIVA.name(),
                        Suscripcion.FrecuenciaPago.MENSUAL.name(),
                        LocalDate.now(),
                        null
                ));
    }

    @Transactional
    public SuscripcionRespuesta activarPremium(
            Usuario usuario,
            String frecuencia) {

        Suscripcion.FrecuenciaPago frecuenciaPago;

        try {
            frecuenciaPago = Suscripcion.FrecuenciaPago.valueOf(frecuencia);
        } catch (Exception e) {
            throw new RuntimeException("La frecuencia de pago no es válida");
        }

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

    @Transactional
    public SuscripcionRespuesta cancelarSuscripcion(Long usuarioId) {

        Suscripcion suscripcion = suscripcionRepository
                .findFirstByUsuarioIdAndEstadoOrderByFechaInicioDesc(
                        usuarioId, Suscripcion.EstadoSuscripcion.ACTIVA)
                .orElseThrow(() -> new RuntimeException("No hay una suscripción activa para cancelar."));

        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.CANCELADA);
        suscripcionRepository.save(suscripcion);

        // Volver a activar el plan GRATIS para que el usuario siempre tenga una suscripción activa
        Plan planGratis = planRepository.findByNombre("GRATIS")
                .orElseThrow(() -> new RuntimeException("No existe el plan GRATIS"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        LocalDate hoy = LocalDate.now();

        Suscripcion suscripcionGratis = new Suscripcion();
        suscripcionGratis.setUsuario(usuario);
        suscripcionGratis.setPlan(planGratis);
        suscripcionGratis.setFrecuenciaPago(Suscripcion.FrecuenciaPago.MENSUAL);
        suscripcionGratis.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcionGratis.setFechaInicio(hoy);

        suscripcionRepository.save(suscripcionGratis);

        return new SuscripcionRespuesta(
                planGratis.getNombre(),
                suscripcionGratis.getEstado().name(),
                suscripcionGratis.getFrecuenciaPago().name(),
                suscripcionGratis.getFechaInicio(),
                suscripcionGratis.getFechaRenovacion()
        );
    }
}