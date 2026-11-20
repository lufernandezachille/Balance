package com.balance.balance.service;

import com.balance.balance.model.Fondo;
import com.balance.balance.model.FondoDetalle;
import com.balance.balance.model.Usuario;
import com.balance.balance.repository.FondoRepository;
import com.balance.balance.repository.FondoDetalleRepository;
import com.balance.balance.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FondoService {

    private final FondoRepository fondoRepository;
    private final FondoDetalleRepository fondoDetalleRepository;
    private final UsuarioRepository usuarioRepository;
    private final CotizacionService cotizacionService;

    public FondoService(
            FondoRepository fondoRepository,
            FondoDetalleRepository fondoDetalleRepository,
            UsuarioRepository usuarioRepository,
            CotizacionService cotizacionService) {

        this.fondoRepository = fondoRepository;
        this.fondoDetalleRepository = fondoDetalleRepository;
        this.usuarioRepository = usuarioRepository;
        this.cotizacionService = cotizacionService;
    }

    public Fondo crearFondo(Fondo fondo) {
        fondo.setEsFondoGeneral(false);
        return fondoRepository.save(fondo);
    }

    @Transactional(readOnly = true)
    public List<Fondo> listarFondos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String monedaBase = usuario.getMonedaPredeterminada() != null
                ? usuario.getMonedaPredeterminada().getCodigo()
                : "ARS";

        List<Fondo> fondos = fondoRepository.findByUsuarioId(usuarioId);

        for (Fondo fondo : fondos) {
            List<FondoDetalle> detalles = fondoDetalleRepository.findByFondoId(fondo.getId());

            Map<String, BigDecimal> porMoneda = new HashMap<>();
            for (FondoDetalle d : detalles) {
                if (d.getCuenta() == null || d.getSaldoAsignado() == null) continue;
                String cod = d.getCuenta().getMoneda().getCodigo();
                porMoneda.put(cod, porMoneda.getOrDefault(cod, BigDecimal.ZERO).add(d.getSaldoAsignado()));
            }

            BigDecimal totalConvertido = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : porMoneda.entrySet()) {
                if (entry.getKey().equals(monedaBase)) {
                    totalConvertido = totalConvertido.add(entry.getValue());
                } else {
                    totalConvertido = totalConvertido.add(
                            cotizacionService.convertir(entry.getValue(), entry.getKey(), monedaBase)
                    );
                }
            }

            fondo.setSaldosPorMoneda(porMoneda);
            fondo.setSaldoTotal(totalConvertido.setScale(2, RoundingMode.HALF_UP));
        }

        return fondos;
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