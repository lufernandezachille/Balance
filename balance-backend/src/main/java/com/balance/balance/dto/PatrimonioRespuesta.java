
package com.balance.balance.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PatrimonioRespuesta {

    private List<CuentaPatrimonio> cuentas;
    private Map<String, BigDecimal> totalesPorMoneda;
    private String monedaBase;
    private BigDecimal totalConvertido;

    public PatrimonioRespuesta() {
    }

    public List<CuentaPatrimonio> getCuentas() {
        return cuentas;
    }

    public void setCuentas(List<CuentaPatrimonio> cuentas) {
        this.cuentas = cuentas;
    }

    public Map<String, BigDecimal> getTotalesPorMoneda() {
        return totalesPorMoneda;
    }

    public void setTotalesPorMoneda(Map<String, BigDecimal> totalesPorMoneda) {
        this.totalesPorMoneda = totalesPorMoneda;
    }

    public String getMonedaBase() {
        return monedaBase;
    }

    public void setMonedaBase(String monedaBase) {
        this.monedaBase = monedaBase;
    }

    public BigDecimal getTotalConvertido() {
        return totalConvertido;
    }

    public void setTotalConvertido(BigDecimal totalConvertido) {
        this.totalConvertido = totalConvertido;
    }

    // Información individual de cada cuenta
    public static class CuentaPatrimonio {

        private Long id;
        private String nombre;
        private String tipo;
        private BigDecimal saldo;
        private String moneda;

        public CuentaPatrimonio() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public BigDecimal getSaldo() {
            return saldo;
        }

        public void setSaldo(BigDecimal saldo) {
            this.saldo = saldo;
        }

        public String getMoneda() {
            return moneda;
        }

        public void setMoneda(String moneda) {
            this.moneda = moneda;
        }
    }
}
