package com.balance.balance.dto;

public class ActualizarPerfilRequest {

    private String nombre;
    private String apellido;
    private String nombreUsuario;
    private String correo;
    private String codigoMoneda;

    public ActualizarPerfilRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getCodigoMoneda() {
        return codigoMoneda;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setCodigoMoneda(String codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }
}