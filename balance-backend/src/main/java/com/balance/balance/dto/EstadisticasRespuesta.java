
package com.balance.balance.dto.estadisticas;

import java.math.BigDecimal;
import java.util.List;

public class EstadisticasRespuesta {

    private ResumenMensual resumen;
    private List<CategoriaEstadistica> gastosPorCategoria;
    private List<CategoriaEstadistica> ingresosPorCategoria;
    private List<EvolucionDiaria> evolucionDiaria;
    private GastosFijosVariables gastosFijosVariables;
    private List<PatrimonioPorTipo> patrimonioPorTipo;
    private List<PatrimonioPorMoneda> patrimonioPorMoneda;
    private List<MetaEstadistica> metas;

    public ResumenMensual getResumen() {
        return resumen;
    }

    public void setResumen(ResumenMensual resumen) {
        this.resumen = resumen;
    }

    public List<CategoriaEstadistica> getGastosPorCategoria() {
        return gastosPorCategoria;
    }

    public void setGastosPorCategoria(List<CategoriaEstadistica> gastosPorCategoria) {
        this.gastosPorCategoria = gastosPorCategoria;
    }

    public List<CategoriaEstadistica> getIngresosPorCategoria() {
        return ingresosPorCategoria;
    }

    public void setIngresosPorCategoria(List<CategoriaEstadistica> ingresosPorCategoria) {
        this.ingresosPorCategoria = ingresosPorCategoria;
    }

    public List<EvolucionDiaria> getEvolucionDiaria() {
        return evolucionDiaria;
    }

    public void setEvolucionDiaria(List<EvolucionDiaria> evolucionDiaria) {
        this.evolucionDiaria = evolucionDiaria;
    }

    public GastosFijosVariables getGastosFijosVariables() {
        return gastosFijosVariables;
    }

    public void setGastosFijosVariables(GastosFijosVariables gastosFijosVariables) {
        this.gastosFijosVariables = gastosFijosVariables;
    }

    public List<PatrimonioPorTipo> getPatrimonioPorTipo() {
        return patrimonioPorTipo;
    }

    public void setPatrimonioPorTipo(List<PatrimonioPorTipo> patrimonioPorTipo) {
        this.patrimonioPorTipo = patrimonioPorTipo;
    }

    public List<PatrimonioPorMoneda> getPatrimonioPorMoneda() {
        return patrimonioPorMoneda;
    }

    public void setPatrimonioPorMoneda(List<PatrimonioPorMoneda> patrimonioPorMoneda) {
        this.patrimonioPorMoneda = patrimonioPorMoneda;
    }

    public List<MetaEstadistica> getMetas() {
        return metas;
    }

    public void setMetas(List<MetaEstadistica> metas) {
        this.metas = metas;
    }

    public static class ResumenMensual {
        private BigDecimal ingresos;
        private BigDecimal gastos;
        private BigDecimal saldo;

        public ResumenMensual(BigDecimal ingresos, BigDecimal gastos, BigDecimal saldo) {
            this.ingresos = ingresos;
            this.gastos = gastos;
            this.saldo = saldo;
        }

        public BigDecimal getIngresos() { return ingresos; }
        public BigDecimal getGastos() { return gastos; }
        public BigDecimal getSaldo() { return saldo; }
    }

    public static class CategoriaEstadistica {
        private String categoria;
        private BigDecimal monto;

        public CategoriaEstadistica(String categoria, BigDecimal monto) {
            this.categoria = categoria;
            this.monto = monto;
        }

        public String getCategoria() { return categoria; }
        public BigDecimal getMonto() { return monto; }
    }

    public static class EvolucionDiaria {
        private int dia;
        private BigDecimal ingresos;
        private BigDecimal gastos;

        public EvolucionDiaria(int dia, BigDecimal ingresos, BigDecimal gastos) {
            this.dia = dia;
            this.ingresos = ingresos;
            this.gastos = gastos;
        }

        public int getDia() { return dia; }
        public BigDecimal getIngresos() { return ingresos; }
        public BigDecimal getGastos() { return gastos; }
    }

    public static class GastosFijosVariables {
        private BigDecimal fijos;
        private BigDecimal variables;

        public GastosFijosVariables(BigDecimal fijos, BigDecimal variables) {
            this.fijos = fijos;
            this.variables = variables;
        }

        public BigDecimal getFijos() { return fijos; }
        public BigDecimal getVariables() { return variables; }
    }

    public static class PatrimonioPorTipo {
        private String tipo;
        private BigDecimal monto;

        public PatrimonioPorTipo(String tipo, BigDecimal monto) {
            this.tipo = tipo;
            this.monto = monto;
        }

        public String getTipo() { return tipo; }
        public BigDecimal getMonto() { return monto; }
    }

    public static class PatrimonioPorMoneda {
        private String moneda;
        private BigDecimal monto;

        public PatrimonioPorMoneda(String moneda, BigDecimal monto) {
            this.moneda = moneda;
            this.monto = monto;
        }

        public String getMoneda() { return moneda; }
        public BigDecimal getMonto() { return monto; }
    }

    public static class MetaEstadistica {
        private Long id;
        private String nombre;
        private BigDecimal acumulado;
        private BigDecimal objetivo;
        private BigDecimal porcentaje;

        public MetaEstadistica(Long id, String nombre, BigDecimal acumulado,
                               BigDecimal objetivo, BigDecimal porcentaje) {
            this.id = id;
            this.nombre = nombre;
            this.acumulado = acumulado;
            this.objetivo = objetivo;
            this.porcentaje = porcentaje;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public BigDecimal getAcumulado() { return acumulado; }
        public BigDecimal getObjetivo() { return objetivo; }
        public BigDecimal getPorcentaje() { return porcentaje; }
    }
}
