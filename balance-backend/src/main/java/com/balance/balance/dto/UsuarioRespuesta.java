
package com.balance.balance.dto;

public class UsuarioRespuesta {

    private Long id;
    private String nombre;
    private String correo;
    private String codigoMoneda;
    private String token;

    public UsuarioRespuesta(Long id, String nombre, String correo,
                            String codigoMoneda, String token) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.codigoMoneda = codigoMoneda;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getCodigoMoneda() {
        return codigoMoneda;
    }

    public String getToken() {
        return token;
    }
}
