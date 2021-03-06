package com.vazquez.miguel.inciapp;

public class IncidenciaRow {

    private int id;
    private String tipo;
    private String estado;
    private String ubicacion;
    private String direccion;

    public IncidenciaRow(int id, String tipo, String estado, String ubicacion, String direccion) {
        this.id = id;
        this.tipo = tipo;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

}
