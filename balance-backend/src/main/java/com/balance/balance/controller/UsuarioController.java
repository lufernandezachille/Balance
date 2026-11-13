
package com.balance.balance.controller;

import com.balance.balance.dto.RegistroUsuarioRequest;
import com.balance.balance.dto.InicioSesionRequest;
import com.balance.balance.dto.UsuarioRespuesta;
import com.balance.balance.model.Usuario;
import com.balance.balance.service.UsuarioService;
import com.balance.balance.service.JwtService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public UsuarioController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public UsuarioRespuesta registrar(@RequestBody RegistroUsuarioRequest datos) {

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre());
        usuario.setCorreo(datos.getCorreo());
        usuario.setContrasenaHash(datos.getContrasena());

        Usuario registrado = usuarioService.registrarUsuario(
                usuario, datos.getCodigoMoneda()
        );

        return convertirRespuesta(registrado);
    }

    @PostMapping("/login")
    public UsuarioRespuesta iniciarSesion(@RequestBody InicioSesionRequest datos) {

        Usuario usuario = usuarioService.autenticarUsuario(
                datos.getCorreo(),
                datos.getContrasena()
        );

        String token = jwtService.generarToken(usuario.getCorreo());

        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getMonedaPredeterminada().getCodigo(),
                token
        );
    }

    private UsuarioRespuesta convertirRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getMonedaPredeterminada().getCodigo(),
                null
        );
    }
}
