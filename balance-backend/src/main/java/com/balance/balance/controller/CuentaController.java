package com.balance.balance.controller;

import com.balance.balance.model.Cuenta;
import com.balance.balance.model.Usuario;
import com.balance.balance.service.CuentaService;
import com.balance.balance.service.UsuarioService;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;

    public CuentaController(CuentaService cuentaService, UsuarioService usuarioService) {
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Cuenta crearCuenta(@RequestBody Cuenta cuenta, Authentication autenticacion) {
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());
        cuenta.setUsuario(usuario);

        return cuentaService.crearCuenta(cuenta);
    }

    @GetMapping
    public List<Cuenta> listarCuentas(Authentication autenticacion) {
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        return cuentaService.listarCuentas(usuario.getId());
    }

    @PutMapping("/{id}")
    public Cuenta actualizarCuenta(
            @PathVariable Long id,
            @RequestBody Cuenta datos,
            Authentication autenticacion) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        return cuentaService.actualizarCuenta(id, usuario.getId(), datos);
    }

    @DeleteMapping("/{id}")
    public String eliminarCuenta(@PathVariable Long id, Authentication autenticacion) {
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        cuentaService.eliminarCuenta(id, usuario.getId());

        return "Cuenta eliminada correctamente";
    }
}
