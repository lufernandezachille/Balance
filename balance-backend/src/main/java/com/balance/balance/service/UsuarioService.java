package com.balance.balance.service;

import com.balance.balance.model.Usuario;
import com.balance.balance.model.Moneda;
import com.balance.balance.repository.UsuarioRepository;
import com.balance.balance.repository.MonedaRepository;
import com.balance.balance.dto.ActualizarPerfilRequest;
import com.balance.balance.model.Plan;
import com.balance.balance.model.Suscripcion;
import com.balance.balance.repository.PlanRepository;
import com.balance.balance.repository.SuscripcionRepository;
import java.time.LocalDate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final MonedaRepository monedaRepository;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();
    private final PlanRepository planRepository;
    private final SuscripcionRepository suscripcionRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            MonedaRepository monedaRepository,
            PlanRepository planRepository,
            SuscripcionRepository suscripcionRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.monedaRepository = monedaRepository;
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
    }


    public Usuario registrarUsuario(Usuario usuario, String codigoMoneda) {
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        if (usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        Moneda moneda = monedaRepository.findByCodigo(codigoMoneda)
                .orElseThrow(() -> new RuntimeException("La moneda no existe"));

        usuario.setMonedaPredeterminada(moneda);
        usuario.setContrasenaHash(codificador.encode(usuario.getContrasenaHash()));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Plan planGratis = planRepository.findByNombre("GRATIS")
                .orElseThrow(() -> new RuntimeException("No existe el plan GRATIS"));

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuario(usuarioGuardado);
        suscripcion.setPlan(planGratis);
        suscripcion.setFrecuenciaPago(Suscripcion.FrecuenciaPago.MENSUAL);
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaInicio(LocalDate.now());

        suscripcionRepository.save(suscripcion);

        return usuarioGuardado;
    }
    
    public Usuario autenticarUsuario(String correo, String contrasena) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("El correo no está registrado"));

        if (!codificador.matches(contrasena, usuario.getContrasenaHash())) {
            throw new RuntimeException("La contraseña es incorrecta");
        }

        return usuario;
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));
    }

    public Usuario actualizarPerfil(Long id, ActualizarPerfilRequest datos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        if (usuarioRepository.existsByCorreoAndIdNot(datos.getCorreo(), id)) {
            throw new RuntimeException("El correo ya está registrado");
        }

        if (usuarioRepository.existsByNombreUsuarioAndIdNot(datos.getNombreUsuario(), id)) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        Moneda moneda = monedaRepository.findByCodigo(datos.getCodigoMoneda())
                .orElseThrow(() -> new RuntimeException("La moneda no existe"));

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setNombreUsuario(datos.getNombreUsuario());
        usuario.setCorreo(datos.getCorreo());
        usuario.setMonedaPredeterminada(moneda);

        return usuarioRepository.save(usuario);
    }
}
