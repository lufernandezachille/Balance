package com.balance.balance.service;

import com.balance.balance.model.Cuenta;
import com.balance.balance.repository.CuentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public Cuenta crearCuenta(Cuenta cuenta) {
        return cuentaRepository.save(cuenta);
    }

    public List<Cuenta> listarCuentas(Long usuarioId) {
        return cuentaRepository.findByUsuarioId(usuarioId);
    }

    public Cuenta buscarCuenta(Long id, Long usuarioId) {
        return cuentaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("La cuenta no existe"));
    }

    public Cuenta actualizarCuenta(Long id, Long usuarioId, Cuenta datos) {
        Cuenta cuenta = buscarCuenta(id, usuarioId);

        cuenta.setNombre(datos.getNombre());
        cuenta.setTipo(datos.getTipo());
        cuenta.setMoneda(datos.getMoneda());
        cuenta.setIncluirEnPatrimonio(datos.isIncluirEnPatrimonio());

        return cuentaRepository.save(cuenta);
    }

    public void eliminarCuenta(Long id, Long usuarioId) {
        Cuenta cuenta = buscarCuenta(id, usuarioId);
        cuentaRepository.delete(cuenta);
    }
}
