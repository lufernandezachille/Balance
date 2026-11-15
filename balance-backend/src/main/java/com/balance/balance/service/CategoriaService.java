package com.balance.balance.service;

import com.balance.balance.model.Categoria;
import com.balance.balance.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria crearCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarCategorias(Long usuarioId) {
        return categoriaRepository.findByUsuarioIdOrUsuarioIsNull(usuarioId);
    }

    public Categoria actualizarCategoria(Long id, Long usuarioId, Categoria datos) {
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("La categoría no existe"));

        categoria.setNombre(datos.getNombre());
        categoria.setTipo(datos.getTipo());
        categoria.setIcono(datos.getIcono());

        return categoriaRepository.save(categoria);
    }

    public void eliminarCategoria(Long id, Long usuarioId) {
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("La categoría no existe"));

        categoriaRepository.delete(categoria);
    }
}
