package com.balance.balance.service;

import com.balance.balance.dto.MetaRequest;
import com.balance.balance.dto.MetaRespuesta;
import com.balance.balance.model.Fondo;
import com.balance.balance.model.FondoDetalle;
import com.balance.balance.model.Usuario;
import com.balance.balance.model.Moneda;
import com.balance.balance.repository.FondoRepository;
import com.balance.balance.repository.FondoDetalleRepository;
import com.balance.balance.repository.MonedaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class MetaService {

    private final FondoRepository fondoRepository;
    private final FondoDetalleRepository fondoDetalleRepository;
    private final MonedaRepository monedaRepository;
    private final UsuarioService usuarioService;
    private final CotizacionService cotizacionService;
    private final FondoService fondoService;
    private final SuscripcionService suscripcionService;

    public MetaService(
            FondoRepository fondoRepository,
            FondoDetalleRepository fondoDetalleRepository,
            MonedaRepository monedaRepository,
            UsuarioService usuarioService,
            CotizacionService cotizacionService,
            FondoService fondoService,
            SuscripcionService suscripcionService) {

        this.fondoRepository = fondoRepository;
        this.fondoDetalleRepository = fondoDetalleRepository;
        this.monedaRepository = monedaRepository;
        this.usuarioService = usuarioService;
        this.cotizacionService = cotizacionService;
        this.fondoService = fondoService;
        this.suscripcionService = suscripcionService;
    }

    // CREAR META
    @Transactional
    public MetaRespuesta crearMeta(String correo, MetaRequest datos) {

        validarDatos(datos);

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        if (!suscripcionService.esPremium(usuario.getId())) {
            int cantidadMetas = fondoRepository.findByUsuarioIdAndEsMetaTrue(usuario.getId()).size();
            if (cantidadMetas >= 3) {
                throw new RuntimeException("LIMITE_PREMIUM: El plan gratuito permite hasta 3 metas de ahorro.");
            }
        }

        Moneda moneda = resolverMoneda(datos.getMonedaId(), usuario);

        Fondo fondo = new Fondo();

        fondo.setUsuario(usuario);
        fondo.setNombre(datos.getNombre());
        fondo.setEsFondoGeneral(false);
        fondo.setEsMeta(true);
        fondo.setMontoObjetivo(datos.getMontoObjetivo());
        fondo.setFechaObjetivo(datos.getFechaObjetivo());
        fondo.setColorIcono(datos.getColorIcono());
        fondo.setMoneda(moneda);

        Fondo metaGuardada = fondoRepository.save(fondo);

        return convertirRespuesta(metaGuardada);
    }

    // LISTAR METAS
    @Transactional(readOnly = true)
    public List<MetaRespuesta> listarMetas(String correo) {

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        List<Fondo> fondos = fondoRepository
                .findByUsuarioIdAndEsMetaTrue(usuario.getId());

        List<MetaRespuesta> metas = new ArrayList<>();

        for (Fondo fondo : fondos) {
            metas.add(convertirRespuesta(fondo));
        }

        return metas;
    }

    // CONSULTAR UNA META
    @Transactional(readOnly = true)
    public MetaRespuesta buscarMeta(Long id, String correo) {

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        Fondo fondo = fondoRepository
                .findByIdAndUsuarioIdAndEsMetaTrue(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("La meta no existe"));

        return convertirRespuesta(fondo);
    }

    // MODIFICAR META
    @Transactional
    public MetaRespuesta actualizarMeta(
            Long id,
            String correo,
            MetaRequest datos) {

        validarDatos(datos);

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        Fondo fondo = fondoRepository
                .findByIdAndUsuarioIdAndEsMetaTrue(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("La meta no existe"));

        Moneda moneda = resolverMoneda(datos.getMonedaId(), usuario);

        fondo.setNombre(datos.getNombre());
        fondo.setMontoObjetivo(datos.getMontoObjetivo());
        fondo.setFechaObjetivo(datos.getFechaObjetivo());
        fondo.setColorIcono(datos.getColorIcono());
        fondo.setMoneda(moneda);

        Fondo metaActualizada = fondoRepository.save(fondo);

        return convertirRespuesta(metaActualizada);
    }

    private Moneda resolverMoneda(Long monedaId, Usuario usuario) {
        if (monedaId == null) {
            return usuario.getMonedaPredeterminada();
        }
        return monedaRepository.findById(monedaId)
                .orElseThrow(() -> new RuntimeException("La moneda seleccionada no existe"));
    }

    // ELIMINAR META
    @Transactional
    public void eliminarMeta(Long id, String correo) {

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        Fondo fondo = fondoRepository
                .findByIdAndUsuarioIdAndEsMetaTrue(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("La meta no existe"));

        // Reutilizamos la eliminación de fondos.
        // El dinero vuelve al fondo General.
        fondoService.eliminarFondo(fondo.getId(), usuario.getId());
    }

    // CALCULAR PROGRESO Y ARMAR RESPUESTA
    private MetaRespuesta convertirRespuesta(Fondo fondo) {

        Moneda monedaMeta = fondo.getMoneda() != null
                ? fondo.getMoneda()
                : fondo.getUsuario().getMonedaPredeterminada();
        String monedaBase = monedaMeta.getCodigo();

        List<FondoDetalle> detalles = fondoDetalleRepository
                .findByFondoId(fondo.getId());

        // Agrupamos el dinero según la moneda de cada cuenta
        Map<String, BigDecimal> totalesPorMoneda = new HashMap<>();

        for (FondoDetalle detalle : detalles) {

            String monedaCuenta = detalle.getCuenta()
                    .getMoneda()
                    .getCodigo();

            BigDecimal saldo = detalle.getSaldoAsignado();

            BigDecimal anterior = totalesPorMoneda
                    .getOrDefault(monedaCuenta, BigDecimal.ZERO);

            totalesPorMoneda.put(
                    monedaCuenta,
                    anterior.add(saldo)
            );
        }

        // Convertimos todos los importes a la moneda de la meta
        BigDecimal montoAcumulado = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entrada
                : totalesPorMoneda.entrySet()) {

            String monedaOrigen = entrada.getKey();
            BigDecimal monto = entrada.getValue();

            if (monedaOrigen.equals(monedaBase)) {

                montoAcumulado = montoAcumulado.add(monto);

            } else {

                BigDecimal convertido = cotizacionService.convertir(
                        monto,
                        monedaOrigen,
                        monedaBase
                );

                montoAcumulado = montoAcumulado.add(convertido);
            }
        }

        montoAcumulado = montoAcumulado.setScale(2, RoundingMode.HALF_UP);

        BigDecimal objetivo = fondo.getMontoObjetivo();

        BigDecimal restante = objetivo.subtract(montoAcumulado);

        if (restante.signum() < 0) {
            restante = BigDecimal.ZERO;
        }

        BigDecimal porcentaje = montoAcumulado
                .multiply(BigDecimal.valueOf(100))
                .divide(objetivo, 2, RoundingMode.HALF_UP);

        if (porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            porcentaje = BigDecimal.valueOf(100);
        }

        // Determinamos el estado de la meta
        String estado;

        if (montoAcumulado.compareTo(objetivo) >= 0) {

            estado = "CUMPLIDA";

        } else if (LocalDate.now().isAfter(fondo.getFechaObjetivo())) {

            estado = "VENCIDA";

        } else {

            estado = "EN_PROGRESO";
        }

        // Armamos la respuesta
        MetaRespuesta respuesta = new MetaRespuesta();

        respuesta.setId(fondo.getId());
        respuesta.setNombre(fondo.getNombre());
        respuesta.setMontoObjetivo(objetivo);
        respuesta.setFechaObjetivo(fondo.getFechaObjetivo());
        respuesta.setColorIcono(fondo.getColorIcono());

        respuesta.setMontoAcumulado(montoAcumulado);
        respuesta.setMontoRestante(restante);
        respuesta.setPorcentajeProgreso(porcentaje);

        respuesta.setMoneda(monedaBase);
        respuesta.setEstado(estado);

        return respuesta;
    }

    // VALIDAR LOS DATOS RECIBIDOS
    private void validarDatos(MetaRequest datos) {

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la meta es obligatorio");
        }

        if (datos.getMontoObjetivo() == null
                || datos.getMontoObjetivo().signum() <= 0) {
            throw new RuntimeException("El monto objetivo debe ser mayor a cero");
        }

        if (datos.getFechaObjetivo() == null) {
            throw new RuntimeException("La fecha límite es obligatoria");
        }
    }
}