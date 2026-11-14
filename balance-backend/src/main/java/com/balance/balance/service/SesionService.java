
package com.balance.balance.service;

import com.balance.balance.model.Sesion;
import com.balance.balance.model.Usuario;
import com.balance.balance.repository.SesionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class SesionService {

    private final SesionRepository sesionRepository;

    @Value("${jwt.expiracion}")
    private long expiracion;

    public SesionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    public void crearSesion(Usuario usuario, String token) {

        Sesion sesion = new Sesion();

        sesion.setUsuario(usuario);
        sesion.setTokenHash(generarHash(token));
        sesion.setFechaExpiracion(
                LocalDateTime.now().plus(Duration.ofMillis(expiracion))
        );

        sesionRepository.save(sesion);
    }

    public boolean sesionActiva(String token) {

        String tokenHash = generarHash(token);

        return sesionRepository.findByTokenHash(tokenHash)
                .map(sesion -> sesion.getFechaExpiracion().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void cerrarSesion(String token) {
        String tokenHash = generarHash(token);
        sesionRepository.deleteByTokenHash(tokenHash);
    }

    private String generarHash(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar el hash del token", e);
        }
    }
}
