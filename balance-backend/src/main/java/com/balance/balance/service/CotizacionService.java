package com.balance.balance.service;

import com.balance.balance.dto.CotizacionRespuesta;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CotizacionService {

    private static final String URL =
            "https://open.er-api.com/v6/latest/USD";

    private final RestTemplate restTemplate = new RestTemplate();

    private CotizacionRespuesta cotizaciones;
    private Instant proximaActualizacion;

    public CotizacionRespuesta obtenerCotizaciones() {

        // Si todavía no tenemos cotizaciones o ya vencieron,
        // consultamos nuevamente la API.
        if (cotizaciones == null ||
                proximaActualizacion == null ||
                Instant.now().isAfter(proximaActualizacion)) {

            cotizaciones = restTemplate.getForObject(
                    URL, CotizacionRespuesta.class
            );

            if (cotizaciones == null ||
                    !"success".equals(cotizaciones.getResult())) {
                throw new RuntimeException(
                        "No se pudieron obtener las cotizaciones."
                );
            }

            proximaActualizacion = ZonedDateTime.parse(
                    cotizaciones.getProximaActualizacion(),
                    DateTimeFormatter.RFC_1123_DATE_TIME
            ).toInstant();
        }

        return cotizaciones;
    }
    
    public java.math.BigDecimal convertir(
            java.math.BigDecimal monto,
            String codigoOrigen,
            String codigoDestino) {

        CotizacionRespuesta datos = obtenerCotizaciones();

        java.math.BigDecimal tasaOrigen =
                datos.getRates().get(codigoOrigen.toUpperCase());

        java.math.BigDecimal tasaDestino =
                datos.getRates().get(codigoDestino.toUpperCase());

        if (tasaOrigen == null || tasaDestino == null) {
            throw new RuntimeException("Moneda no disponible.");
        }

        // Primero convertimos el importe a USD.
        java.math.BigDecimal montoEnDolares =
                monto.divide(tasaOrigen, 10,
                        java.math.RoundingMode.HALF_UP);

        // Después convertimos de USD a la moneda destino.
        return montoEnDolares.multiply(tasaDestino)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

}
