package com.balance.balance.service;

import com.balance.balance.model.Fondo;
import com.balance.balance.repository.FondoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FondoService {

    private final FondoRepository fondoRepository;

    public FondoService(FondoRepository fondoRepository) {
        this.fondoRepository = fondoRepository;
    }

    public Fondo crearFondo(Fondo fondo) {
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
        fondo.setMoneda(datos.getMoneda());
        fondo.setEsMeta(datos.isEsMeta());
        fondo.setMontoObjetivo(datos.getMontoObjetivo());
        fondo.setFechaObjetivo(datos.getFechaObjetivo());
        fondo.setColorIcono(datos.getColorIcono());

        return fondoRepository.save(fondo);
    }

    public void eliminarFondo(Long id, Long usuarioId) {
        Fondo fondo = buscarFondo(id, usuarioId);

        if (fondo.isEsFondoGeneral()) {
            throw new RuntimeException("No se puede eliminar el fondo general");
        }

        fondoRepository.delete(fondo);
    }
}
