package com.balance.balance.controller;

import com.balance.balance.model.Movimiento;
import com.balance.balance.model.Usuario;
import com.balance.balance.service.MovimientoService;
import com.balance.balance.service.UsuarioService;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final UsuarioService usuarioService;

    public MovimientoController(
            MovimientoService movimientoService,
            UsuarioService usuarioService) {
        this.movimientoService = movimientoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Movimiento crearMovimiento(
            @RequestBody Movimiento movimiento,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(authentication.getName());
        movimiento.setUsuario(usuario);

        return movimientoService.crearMovimiento(movimiento, usuario.getId());
    }

    @GetMapping
    public List<Movimiento> listarMovimientos(Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorCorreo(authentication.getName());

        return movimientoService.listarMovimientos(usuario.getId());
    }

    @PutMapping("/{id}")
    public Movimiento actualizarMovimiento(
            @PathVariable Long id,
            @RequestBody Movimiento movimiento,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(authentication.getName());

        return movimientoService.actualizarMovimiento(
                id, movimiento, usuario.getId());
    }

    @DeleteMapping("/{id}")
    public void eliminarMovimiento(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(authentication.getName());

        movimientoService.eliminarMovimiento(id, usuario.getId());
    }
}