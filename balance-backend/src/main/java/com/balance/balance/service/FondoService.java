
package com.balance.balance.service;

import com.balance.balance.model.Fondo;
import com.balance.balance.model.FondoDetalle;
import com.balance.balance.repository.FondoRepository;
import com.balance.balance.repository.FondoDetalleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FondoService {

    private final FondoRepository fondoRepository;
    private final FondoDetalleRepository fondoDetalleRepository;

    public FondoService(
            FondoRepository fondoRepository,
            FondoDetalleRepository fondoDetalleRepository) {

        this.fondoRepository = fondoRepository;
        this.fondoDetalleRepository = fondoDetalleRepository;
    }

    public Fondo crearFondo(Fondo fondo) {
        fondo.setEsFondoGeneral(false);
        return fondoRepository.save(fondo);
    }

    public List<Fondo> listarFondos(Long usuarioId) {
        return fondoRepository.findByUsuarioId(usuarioId);
    }

    public Fondo buscarFondo(Long id, Long usuarioId) {
        return fondoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("El fondo no existe"));
    }

    public Fondo actualizarFondo(Long id, Long usuarioId, Fondo datos) {

        Fondo fondo = buscarFondo(id, usuarioId);

        if (fondo.isEsFondoGeneral()) {
            throw new RuntimeException("No se puede modificar el fondo general");
        }

        fondo.setNombre(datos.getNombre());
        fondo.setEsMeta(datos.isEsMeta());
        fondo.setMontoObjetivo(datos.getMontoObjetivo());
        fondo.setFechaObjetivo(datos.getFechaObjetivo());
        fondo.setColorIcono(datos.getColorIcono());

        return fondoRepository.save(fondo);
    }

    @Transactional
    public void eliminarFondo(Long id, Long usuarioId) {

        Fondo fondo = buscarFondo(id, usuarioId);

        if (fondo.isEsFondoGeneral()) {
            throw new RuntimeException("No se puede eliminar el fondo general");
        }

        Fondo general = fondoRepository
                .findByUsuarioIdAndEsFondoGeneralTrue(usuarioId)
                .orElseThrow(() -> new RuntimeException("No se encontró el fondo General"));

        List<FondoDetalle> detalles = fondoDetalleRepository
                .findByFondoId(fondo.getId());

        for (FondoDetalle detalle : detalles) {

            FondoDetalle detalleGeneral = fondoDetalleRepository
                    .findByFondoIdAndCuentaId(
                            general.getId(),
                            detalle.getCuenta().getId()
                    )
                    .orElse(null);

            if (detalleGeneral == null) {
                detalleGeneral = new FondoDetalle();
                detalleGeneral.setFondo(general);
                detalleGeneral.setCuenta(detalle.getCuenta());
                detalleGeneral.setSaldoAsignado(detalle.getSaldoAsignado());
            } else {
                detalleGeneral.setSaldoAsignado(
                        detalleGeneral.getSaldoAsignado()
                                .add(detalle.getSaldoAsignado())
                );
            }

            fondoDetalleRepository.save(detalleGeneral);
        }

        fondoDetalleRepository.deleteAll(detalles);
        fondoRepository.delete(fondo);
    }
}
