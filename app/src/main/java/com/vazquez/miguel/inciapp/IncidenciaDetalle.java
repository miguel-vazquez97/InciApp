package com.vazquez.miguel.inciapp;

import android.graphics.Bitmap;

import java.time.LocalDate;

public class IncidenciaDetalle {

    private String estado;
    private String ubicacion;
    private String direccion;
    private String descripcion;
    private String tipo;
    private LocalDate date;
    private String descripcionEstadoIncidencia;
    private Bitmap image;


    public IncidenciaDetalle(String estado, String ubicacion, String direccion, String descripcion, String tipo, LocalDate date, String descripcionEstadoIncidencia,  Bitmap image) {
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.date = date;
        this.descripcionEstadoIncidencia = descripcionEstadoIncidencia;
        this.image = image;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescripcionEstadoIncidencia() {
        return descripcionEstadoIncidencia;
    }

    public void setDescripcionEstadoIncidencia(String descripcionEstadoIncidencia) {
        this.descripcionEstadoIncidencia = descripcionEstadoIncidencia;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
