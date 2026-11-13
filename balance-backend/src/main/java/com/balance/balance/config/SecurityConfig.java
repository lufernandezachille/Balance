package com.balance.balance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configurarSeguridad(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(autorizacion -> autorizacion

                        .requestMatchers(HttpMethod.POST,
                                "/api/usuarios/registro",
                                "/api/usuarios/login").permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
