package com.balance.balance.controller;

import com.balance.balance.model.Categoria;
import com.balance.balance.model.Usuario;
import com.balance.balance.service.CategoriaService;
import com.balance.balance.service.UsuarioService;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;

    public CategoriaController(CategoriaService categoriaService, UsuarioService usuarioService) {
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Categoria crearCategoria(
            @RequestBody Categoria categoria,
            Authentication autenticacion) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());
        categoria.setUsuario(usuario);

        return categoriaService.crearCategoria(categoria);
    }

    @GetMapping
    public List<Categoria> listarCategorias(Authentication autenticacion) {
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        return categoriaService.listarCategorias(usuario.getId());
    }

    @PutMapping("/{id}")
    public Categoria actualizarCategoria(
            @PathVariable Long id,
            @RequestBody Categoria datos,
            Authentication autenticacion) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        return categoriaService.actualizarCategoria(id, usuario.getId(), datos);
    }

    @DeleteMapping("/{id}")
    public String eliminarCategoria(
            @PathVariable Long id,
            Authentication autenticacion) {

        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());

        categoriaService.eliminarCategoria(id, usuario.getId());

        return "Categoría eliminada correctamente";
    }
}
