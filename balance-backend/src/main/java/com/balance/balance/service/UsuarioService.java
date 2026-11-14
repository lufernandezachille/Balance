package com.balance.balance.service;

import com.balance.balance.model.Usuario;
import com.balance.balance.model.Moneda;
import com.balance.balance.repository.UsuarioRepository;
import com.balance.balance.repository.MonedaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final MonedaRepository monedaRepository;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository,
                          MonedaRepository monedaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.monedaRepository = monedaRepository;
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

        return usuarioRepository.save(usuario);
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
}
