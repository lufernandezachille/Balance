package com.balance.balance.controller;

import com.balance.balance.dto.RegistroUsuarioRequest;
import com.balance.balance.dto.InicioSesionRequest;
import com.balance.balance.dto.UsuarioRespuesta;
import com.balance.balance.model.Usuario;
import com.balance.balance.service.UsuarioService;
import com.balance.balance.service.JwtService;
import com.balance.balance.service.SesionService;
import com.balance.balance.dto.ActualizarPerfilRequest;
import com.balance.balance.dto.SuscripcionRespuesta;
import com.balance.balance.service.SuscripcionService;
import com.balance.balance.dto.ActivarPremiumRequest;
import com.balance.balance.dto.SuscripcionRespuesta;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final SesionService sesionService;
    private final SuscripcionService suscripcionService;

    public UsuarioController(UsuarioService usuarioService,
                             JwtService jwtService,
                             SesionService sesionService,
                             SuscripcionService suscripcionService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.sesionService = sesionService;
        this.suscripcionService = suscripcionService;
    }

    @PostMapping("/registro")
    public UsuarioRespuesta registrar(@RequestBody RegistroUsuarioRequest datos) {

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setNombreUsuario(datos.getNombreUsuario());
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

        sesionService.crearSesion(usuario, token);

        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getNombreUsuario(),
                usuario.getCorreo(),
                usuario.getMonedaPredeterminada().getCodigo(),
                token
        );
    }

    private UsuarioRespuesta convertirRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getNombreUsuario(),
                usuario.getCorreo(),
                usuario.getMonedaPredeterminada().getCodigo(),
                null
        );
    }

    @PostMapping("/logout")
    public String cerrarSesion(
            @RequestHeader("Authorization") String encabezado) {

        String token = encabezado.substring(7);

        sesionService.cerrarSesion(token);

        return "Sesión cerrada correctamente";
    }

    @GetMapping("/perfil")
    public UsuarioRespuesta obtenerPerfil(Authentication autenticacion) {
        String correo = autenticacion.getName();

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        return convertirRespuesta(usuario);
    }

    @PutMapping("/perfil")
    public UsuarioRespuesta actualizarPerfil(
            @RequestBody ActualizarPerfilRequest datos,
            Authentication autenticacion) {

        String correo = autenticacion.getName();

        Usuario usuarioActual = usuarioService.buscarPorCorreo(correo);

        Usuario actualizado = usuarioService.actualizarPerfil(
                usuarioActual.getId(), datos
        );

        return convertirRespuesta(actualizado);
    }

    @GetMapping("/suscripcion")
    public SuscripcionRespuesta obtenerSuscripcion(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            String correo) {

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        return suscripcionService.obtenerSuscripcionActual(usuario.getId());
    }

    @PostMapping("/suscripcion/premium")
    public SuscripcionRespuesta activarPremium(
            @AuthenticationPrincipal String correo,
            @RequestBody ActivarPremiumRequest request) {

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        return suscripcionService.activarPremium(
                usuario,
                request.getFrecuenciaPago()
        );
    }
}
