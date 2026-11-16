
package com.balance.balance.controller;

import com.balance.balance.dto.MetaRequest;
import com.balance.balance.dto.MetaRespuesta;
import com.balance.balance.service.MetaService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    // CREAR META
    @PostMapping
    public MetaRespuesta crearMeta(
            @RequestBody MetaRequest datos,
            Authentication autenticacion) {

        return metaService.crearMeta(
                autenticacion.getName(),
                datos
        );
    }

    // LISTAR METAS
    @GetMapping
    public List<MetaRespuesta> listarMetas(Authentication autenticacion) {

        return metaService.listarMetas(autenticacion.getName());
    }

    // CONSULTAR UNA META
    @GetMapping("/{id}")
    public MetaRespuesta buscarMeta(
            @PathVariable Long id,
            Authentication autenticacion) {

        return metaService.buscarMeta(id, autenticacion.getName());
    }

    // MODIFICAR META
    @PutMapping("/{id}")
    public MetaRespuesta actualizarMeta(
            @PathVariable Long id,
            @RequestBody MetaRequest datos,
            Authentication autenticacion) {

        return metaService.actualizarMeta(
                id,
                autenticacion.getName(),
                datos
        );
    }

    // ELIMINAR META
    @DeleteMapping("/{id}")
    public String eliminarMeta(
            @PathVariable Long id,
            Authentication autenticacion) {

        metaService.eliminarMeta(id, autenticacion.getName());

        return "Meta eliminada correctamente";
    }
}
