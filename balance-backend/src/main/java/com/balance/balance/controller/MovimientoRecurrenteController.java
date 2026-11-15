
package com.balance.balance.controller;

import com.balance.balance.model.*;
import com.balance.balance.service.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos-recurrentes")
public class MovimientoRecurrenteController {

    private final MovimientoRecurrenteService recurrenteService;
    private final UsuarioService usuarioService;

    public MovimientoRecurrenteController(
            MovimientoRecurrenteService recurrenteService,
            UsuarioService usuarioService) {

        this.recurrenteService = recurrenteService;
        this.usuarioService = usuarioService;
    }

    // CREAR MOVIMIENTO RECURRENTE

    @PostMapping
    public MovimientoRecurrente crear(
            @RequestBody MovimientoRecurrente recurrente,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(
                authentication.getName());

        recurrente.setUsuario(usuario);

        return recurrenteService.crear(recurrente, usuario.getId());
    }

    // LISTAR MOVIMIENTOS RECURRENTES

    @GetMapping
    public List<MovimientoRecurrente> listar(
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(
                authentication.getName());

        return recurrenteService.listar(usuario.getId());
    }

    // MODIFICAR MOVIMIENTO RECURRENTE

    @PutMapping("/{id}")
    public MovimientoRecurrente actualizar(
            @PathVariable Long id,
            @RequestBody MovimientoRecurrente datos,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(
                authentication.getName());

        return recurrenteService.actualizar(
                id, datos, usuario.getId());
    }

    // ELIMINAR MOVIMIENTO RECURRENTE

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(
                authentication.getName());

        recurrenteService.eliminar(id, usuario.getId());
    }

    // AGREGAR EXCEPCION

    @PostMapping("/{id}/excepciones")
    public ExcepcionMovimientoRecurrente agregarExcepcion(
            @PathVariable Long id,
            @RequestBody ExcepcionMovimientoRecurrente excepcion,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(
                authentication.getName());

        return recurrenteService.agregarExcepcion(
                id, excepcion.getFecha(), usuario.getId());
    }

    // LISTAR EXCEPCIONES

    @GetMapping("/{id}/excepciones")
    public List<ExcepcionMovimientoRecurrente> listarExcepciones(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(
                authentication.getName());

        return recurrenteService.listarExcepciones(
                id, usuario.getId());
    }

    // ELIMINAR EXCEPCION

    @DeleteMapping("/{id}/excepciones")
    public void eliminarExcepcion(
            @PathVariable Long id,
            @RequestParam LocalDate fecha,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorCorreo(
                authentication.getName());

        recurrenteService.eliminarExcepcion(
                id, fecha, usuario.getId());
    }
}
