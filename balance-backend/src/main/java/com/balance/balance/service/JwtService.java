package com.balance.balance.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String claveSecreta;

    @Value("${jwt.expiracion}")
    private long expiracion;

    private SecretKey obtenerClave() {
        byte[] clave = Decoders.BASE64.decode(claveSecreta);
        return Keys.hmacShaKeyFor(clave);
    }

    public String generarToken(String correo) {
        Date ahora = new Date();
        Date vencimiento = new Date(ahora.getTime() + expiracion);

        return Jwts.builder()
                .subject(correo)
                .issuedAt(ahora)
                .expiration(vencimiento)
                .signWith(obtenerClave())
                .compact();
    }

    public String extraerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }

    public boolean validarToken(String token, String correo) {
        try {
            String correoToken = extraerCorreo(token);
            return correoToken.equals(correo);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
