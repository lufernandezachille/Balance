package com.balance.balance.controller;

import com.balance.balance.model.Fondo;
import com.balance.balance.model.Usuario;
import com.balance.balance.service.FondoService;
import com.balance.balance.service.UsuarioService;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/fondos")
public class FondoController {

    private final FondoService fondoService;
    private final UsuarioService usuarioService;

    public FondoController(FondoService fondoService, UsuarioService usuarioService) {
        this.fondoService = fondoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Fondo crearFondo(@RequestBody Fondo fondo, Authentication autenticacion) {
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());
        fondo.setUsuario(usuario);

        return fondoService.crearFondo(fondo);
    }

    @GetMapping
    public List<Fondo> listarFondos(Authentication autenticacion) {
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        return fondoService.listarFondos(usuario.getId());
    }

    @PutMapping("/{id}")
    public Fondo actualizarFondo(
            @PathVariable Long id,
            @RequestBody Fondo datos,
            Authentication autenticacion) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        return fondoService.actualizarFondo(id, usuario.getId(), datos);
    }

    @DeleteMapping("/{id}")
    public String eliminarFondo(@PathVariable Long id, Authentication autenticacion) {
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        fondoService.eliminarFondo(id, usuario.getId());

        return "Fondo eliminado correctamente";
    }
}
