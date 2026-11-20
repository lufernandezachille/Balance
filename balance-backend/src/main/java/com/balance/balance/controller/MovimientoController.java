package com.balance.balance.controller;

import com.balance.balance.model.Movimiento;
import com.balance.balance.model.Usuario;
import com.balance.balance.service.MovimientoRecurrenteService;
import com.balance.balance.service.MovimientoService;
import com.balance.balance.service.UsuarioService;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final MovimientoRecurrenteService recurrenteService;
    private final UsuarioService usuarioService;

    public MovimientoController(
            MovimientoService movimientoService,
            MovimientoRecurrenteService recurrenteService,
            UsuarioService usuarioService) {
        this.movimientoService = movimientoService;
        this.recurrenteService = recurrenteService;
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
    public List<Movimiento> listarMovimientos(
            @RequestParam(required = false) Movimiento.TipoMovimiento tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long cuentaId,
            @RequestParam(required = false) Long fondoId,
            @RequestParam(required = false) Movimiento.MedioPago medioPago,
            @RequestParam(required = false) Long monedaId,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate fechaDesde,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate fechaHasta,
            @RequestParam(required = false) BigDecimal montoMinimo,
            @RequestParam(required = false) BigDecimal montoMaximo,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(authentication.getName());

        recurrenteService.procesarVencidos();

        return movimientoService.filtrarMovimientos(
                usuario.getId(),
                tipo,
                categoriaId,
                cuentaId,
                fondoId,
                medioPago,
                monedaId,
                fechaDesde,
                fechaHasta,
                montoMinimo,
                montoMaximo
        );
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