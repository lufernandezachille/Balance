
package com.balance.balance.service;

import com.balance.balance.dto.MetaRespuesta;
import com.balance.balance.dto.EstadisticasRespuesta;
import com.balance.balance.model.Cuenta;
import com.balance.balance.model.Movimiento;
import com.balance.balance.model.Usuario;
import com.balance.balance.repository.CuentaRepository;
import com.balance.balance.repository.MovimientoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstadisticasService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final UsuarioService usuarioService;
    private final CotizacionService cotizacionService;
    private final MetaService metaService;

    public EstadisticasService(
            MovimientoRepository movimientoRepository,
            CuentaRepository cuentaRepository,
            UsuarioService usuarioService,
            CotizacionService cotizacionService,
            MetaService metaService) {

        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
        this.usuarioService = usuarioService;
        this.cotizacionService = cotizacionService;
        this.metaService = metaService;
    }

    @Transactional(readOnly = true)
    public EstadisticasRespuesta obtenerEstadisticas(
            String correo, Integer mes, Integer anio) {

        LocalDate hoy = LocalDate.now();

        if (mes == null) {
            mes = hoy.getMonthValue();
        }

        if (anio == null) {
            anio = hoy.getYear();
        }

        if (mes < 1 || mes > 12) {
            throw new RuntimeException("El mes debe estar entre 1 y 12");
        }

        if (anio < 1900 || anio > 2100) {
            throw new RuntimeException("El año ingresado no es válido");
        }

        YearMonth periodo = YearMonth.of(anio, mes);
        LocalDate fechaDesde = periodo.atDay(1);
        LocalDate fechaHasta = periodo.atEndOfMonth();

        Usuario usuario = usuarioService.buscarPorCorreo(correo);
        Long usuarioId = usuario.getId();

        String monedaBase = usuario.getMonedaPredeterminada().getCodigo();

        List<Movimiento> movimientos =
                movimientoRepository.findByUsuarioId(usuarioId);

        // Datos del resumen mensual
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;

        // Agrupación por categoría
        Map<String, BigDecimal> gastosPorCategoria = new LinkedHashMap<>();
        Map<String, BigDecimal> ingresosPorCategoria = new LinkedHashMap<>();

        // Evolución diaria
        Map<Integer, BigDecimal> ingresosDiarios = new LinkedHashMap<>();
        Map<Integer, BigDecimal> gastosDiarios = new LinkedHashMap<>();

        // Gastos fijos y variables
        BigDecimal gastosFijos = BigDecimal.ZERO;
        BigDecimal gastosVariables = BigDecimal.ZERO;

        // Inicializamos todos los días del mes
        for (int dia = 1; dia <= periodo.lengthOfMonth(); dia++) {
            ingresosDiarios.put(dia, BigDecimal.ZERO);
            gastosDiarios.put(dia, BigDecimal.ZERO);
        }

        // Recorremos los movimientos del usuario
        for (Movimiento movimiento : movimientos) {

            if (movimiento.getFecha() == null
                    || movimiento.getFecha().isBefore(fechaDesde)
                    || movimiento.getFecha().isAfter(fechaHasta)) {
                continue;
            }

            if (movimiento.getTipo() == Movimiento.TipoMovimiento.TRANSFERENCIA) {
                continue;
            }

            BigDecimal monto = convertirAMonedaBase(
                    movimiento.getMonto(),
                    movimiento.getMoneda().getCodigo(),
                    monedaBase
            );

            int dia = movimiento.getFecha().getDayOfMonth();

            String categoria = movimiento.getCategoria() != null
                    ? movimiento.getCategoria().getNombre()
                    : "Sin categoría";

            if (movimiento.getTipo() == Movimiento.TipoMovimiento.INGRESO) {

                ingresos = ingresos.add(monto);

                ingresosPorCategoria.merge(
                        categoria, monto, BigDecimal::add
                );

                ingresosDiarios.merge(
                        dia, monto, BigDecimal::add
                );

            } else if (movimiento.getTipo() == Movimiento.TipoMovimiento.GASTO) {

                gastos = gastos.add(monto);

                gastosPorCategoria.merge(
                        categoria, monto, BigDecimal::add
                );

                gastosDiarios.merge(
                        dia, monto, BigDecimal::add
                );

                if (movimiento.isEsRecurrente()) {
                    gastosFijos = gastosFijos.add(monto);
                } else {
                    gastosVariables = gastosVariables.add(monto);
                }
            }
        }

        // Armamos el resumen mensual
        BigDecimal saldo = ingresos.subtract(gastos);

        EstadisticasRespuesta.ResumenMensual resumen =
                new EstadisticasRespuesta.ResumenMensual(
                        redondear(ingresos),
                        redondear(gastos),
                        redondear(saldo)
                );

        // Convertimos las categorías en listas
        List<EstadisticasRespuesta.CategoriaEstadistica> listaGastos =
                convertirCategorias(gastosPorCategoria);

        List<EstadisticasRespuesta.CategoriaEstadistica> listaIngresos =
                convertirCategorias(ingresosPorCategoria);

        // Armamos la evolución diaria
        List<EstadisticasRespuesta.EvolucionDiaria> evolucion =
                new ArrayList<>();

        for (int dia = 1; dia <= periodo.lengthOfMonth(); dia++) {
            evolucion.add(
                    new EstadisticasRespuesta.EvolucionDiaria(
                            dia,
                            redondear(ingresosDiarios.get(dia)),
                            redondear(gastosDiarios.get(dia))
                    )
            );
        }

        // Gastos fijos y variables
        EstadisticasRespuesta.GastosFijosVariables fijosVariables =
                new EstadisticasRespuesta.GastosFijosVariables(
                        redondear(gastosFijos),
                        redondear(gastosVariables)
                );

        // Patrimonio actual
        List<Cuenta> cuentas =
                cuentaRepository.findByUsuarioIdAndIncluirEnPatrimonioTrue(usuarioId);

        Map<String, BigDecimal> patrimonioPorTipo = new LinkedHashMap<>();
        Map<String, BigDecimal> patrimonioPorMoneda = new LinkedHashMap<>();

        for (Cuenta cuenta : cuentas) {

            String tipo = cuenta.getTipo().name();
            String moneda = cuenta.getMoneda().getCodigo();

            BigDecimal saldoCuenta = cuenta.getSaldoActual();

            // Distribución por moneda: se conserva el importe original
            patrimonioPorMoneda.merge(
                    moneda, saldoCuenta, BigDecimal::add
            );

            // Distribución por tipo: se convierte a moneda base
            BigDecimal saldoConvertido = convertirAMonedaBase(
                    saldoCuenta, moneda, monedaBase
            );

            patrimonioPorTipo.merge(
                    tipo, saldoConvertido, BigDecimal::add
            );
        }

        List<EstadisticasRespuesta.PatrimonioPorTipo> listaPatrimonioTipo =
                new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entrada : patrimonioPorTipo.entrySet()) {
            listaPatrimonioTipo.add(
                    new EstadisticasRespuesta.PatrimonioPorTipo(
                            entrada.getKey(),
                            redondear(entrada.getValue())
                    )
            );
        }

        List<EstadisticasRespuesta.PatrimonioPorMoneda> listaPatrimonioMoneda =
                new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entrada : patrimonioPorMoneda.entrySet()) {
            listaPatrimonioMoneda.add(
                    new EstadisticasRespuesta.PatrimonioPorMoneda(
                            entrada.getKey(),
                            redondear(entrada.getValue())
                    )
            );
        }

        // Reutilizamos el cálculo de progreso de las metas
        List<MetaRespuesta> metasUsuario = metaService.listarMetas(correo);

        List<EstadisticasRespuesta.MetaEstadistica> listaMetas =
                new ArrayList<>();

        for (MetaRespuesta meta : metasUsuario) {
            listaMetas.add(
                    new EstadisticasRespuesta.MetaEstadistica(
                            meta.getId(),
                            meta.getNombre(),
                            meta.getMontoAcumulado(),
                            meta.getMontoObjetivo(),
                            meta.getPorcentajeProgreso()
                    )
            );
        }

        // Construimos la respuesta completa
        EstadisticasRespuesta respuesta = new EstadisticasRespuesta();

        respuesta.setResumen(resumen);
        respuesta.setGastosPorCategoria(listaGastos);
        respuesta.setIngresosPorCategoria(listaIngresos);
        respuesta.setEvolucionDiaria(evolucion);
        respuesta.setGastosFijosVariables(fijosVariables);
        respuesta.setPatrimonioPorTipo(listaPatrimonioTipo);
        respuesta.setPatrimonioPorMoneda(listaPatrimonioMoneda);
        respuesta.setMetas(listaMetas);

        return respuesta;
    }

    private BigDecimal convertirAMonedaBase(
            BigDecimal monto, String monedaOrigen, String monedaBase) {

        if (monto == null) {
            return BigDecimal.ZERO;
        }

        if (monedaOrigen.equals(monedaBase)) {
            return monto;
        }

        return cotizacionService.convertir(
                monto, monedaOrigen, monedaBase
        );
    }

    private BigDecimal redondear(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    private List<EstadisticasRespuesta.CategoriaEstadistica> convertirCategorias(
            Map<String, BigDecimal> categorias) {

        List<EstadisticasRespuesta.CategoriaEstadistica> resultado =
                new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entrada : categorias.entrySet()) {
            resultado.add(
                    new EstadisticasRespuesta.CategoriaEstadistica(
                            entrada.getKey(),
                            redondear(entrada.getValue())
                    )
            );
        }

        return resultado;
    }
}
