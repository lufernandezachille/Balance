package com.balance.balance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public class CotizacionRespuesta {

    private String result;

    @JsonProperty("base_code")
    private String baseCode;

    private Map<String, BigDecimal> rates;

    @JsonProperty("time_last_update_utc")
    private String ultimaActualizacion;

    @JsonProperty("time_next_update_utc")
    private String proximaActualizacion;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(String baseCode) {
        this.baseCode = baseCode;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    public void setRates(Map<String, BigDecimal> rates) {
        this.rates = rates;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(String ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public String getProximaActualizacion() {
        return proximaActualizacion;
    }

    public void setProximaActualizacion(String proximaActualizacion) {
        this.proximaActualizacion = proximaActualizacion;
    }
}
