package com.balance.balance.config;

import com.balance.balance.model.Usuario;
import com.balance.balance.repository.UsuarioRepository;
import com.balance.balance.service.JwtService;
import com.balance.balance.service.SesionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final SesionService sesionService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UsuarioRepository usuarioRepository,
                                   SesionService sesionService) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.sesionService = sesionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = encabezado.substring(7);

        try {
            String correo = jwtService.extraerCorreo(token);

            Usuario usuario = usuarioRepository.findByCorreo(correo)
                    .orElse(null);

            if (usuario != null
                    && Boolean.TRUE.equals(usuario.getActivo())
                    && jwtService.validarToken(token, correo)
                    && sesionService.sesionActiva(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(
                                correo,
                                null,
                                List.of()
                        );

                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }

        } catch (Exception e) {
        }

        filterChain.doFilter(request, response);
    }
}
