
package com.balance.balance.service;

import com.balance.balance.dto.PatrimonioRespuesta;
import com.balance.balance.dto.PatrimonioRespuesta.CuentaPatrimonio;
import com.balance.balance.model.Cuenta;
import com.balance.balance.model.Usuario;
import com.balance.balance.repository.CuentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatrimonioService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioService usuarioService;
    private final CotizacionService cotizacionService;

    public PatrimonioService(
            CuentaRepository cuentaRepository,
            UsuarioService usuarioService,
            CotizacionService cotizacionService) {

        this.cuentaRepository = cuentaRepository;
        this.usuarioService = usuarioService;
        this.cotizacionService = cotizacionService;
    }

    @Transactional(readOnly = true)
    public PatrimonioRespuesta calcularPatrimonio(String correo) {

        // Buscamos al usuario que inició sesión
        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        // Obtenemos la moneda predeterminada
        String monedaBase = usuario.getMonedaPredeterminada().getCodigo();

        // Buscamos solamente las cuentas incluidas en el patrimonio
        List<Cuenta> cuentas = cuentaRepository
                .findByUsuarioIdAndIncluirEnPatrimonioTrue(usuario.getId());

        // Preparamos la respuesta
        PatrimonioRespuesta respuesta = new PatrimonioRespuesta();

        List<CuentaPatrimonio> cuentasRespuesta = new ArrayList<>();

        // Acumulamos los saldos separados por moneda
        Map<String, BigDecimal> totalesPorMoneda = new HashMap<>();

        // Inicializamos el total convertido
        BigDecimal totalConvertido = BigDecimal.ZERO;

        // Recorremos todas las cuentas
        for (Cuenta cuenta : cuentas) {

            String codigoMoneda = cuenta.getMoneda().getCodigo();

            BigDecimal saldo = cuenta.getSaldoActual();

            // Agregamos la información de la cuenta
            CuentaPatrimonio cuentaPatrimonio = new CuentaPatrimonio();

            cuentaPatrimonio.setId(cuenta.getId());
            cuentaPatrimonio.setNombre(cuenta.getNombre());
            cuentaPatrimonio.setTipo(cuenta.getTipo().name());
            cuentaPatrimonio.setSaldo(saldo);
            cuentaPatrimonio.setMoneda(codigoMoneda);

            cuentasRespuesta.add(cuentaPatrimonio);

            // Sumamos el saldo al total de su moneda
            BigDecimal totalAnterior = totalesPorMoneda
                    .getOrDefault(codigoMoneda, BigDecimal.ZERO);

            totalesPorMoneda.put(
                    codigoMoneda,
                    totalAnterior.add(saldo)
            );

        }

        // Convertimos los totales de cada moneda a la moneda predeterminada
        for (Map.Entry<String, BigDecimal> entrada : totalesPorMoneda.entrySet()) {

            String monedaOrigen = entrada.getKey();
            BigDecimal monto = entrada.getValue();

            if (monedaOrigen.equals(monedaBase)) {

                totalConvertido = totalConvertido.add(monto);

            } else {

                BigDecimal montoConvertido = cotizacionService.convertir(
                        monto,
                        monedaOrigen,
                        monedaBase
                );

                totalConvertido = totalConvertido.add(montoConvertido);
            }
        }

        // Completamos la respuesta
        respuesta.setCuentas(cuentasRespuesta);
        respuesta.setTotalesPorMoneda(totalesPorMoneda);
        respuesta.setMonedaBase(monedaBase);
        respuesta.setTotalConvertido(totalConvertido);

        return respuesta;
    }
}
