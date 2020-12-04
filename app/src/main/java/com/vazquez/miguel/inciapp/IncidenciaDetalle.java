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

    private String descripcionArreglo;
    private String fechaArreglo;
    private Bitmap imageArreglo;


    public IncidenciaDetalle(String estado, String ubicacion, String direccion, String descripcion, String tipo, LocalDate date,  Bitmap image) {
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.date = date;
        this.image = image;
    }

    public IncidenciaDetalle(String estado, String ubicacion, String direccion, String descripcion, String tipo, LocalDate date, String descripcionEstado, Bitmap image) {
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.date = date;
        this.descripcionEstadoIncidencia = descripcionEstado;
        this.image = image;
    }

    public IncidenciaDetalle(String estado, String ubicacion, String direccion, String descripcion, String tipo, LocalDate date, Bitmap image, String descripcionArreglo, String fechaArreglo, Bitmap imageArreglo) {
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.date = date;
        this.image = image;
        this.descripcionArreglo = descripcionArreglo;
        this.fechaArreglo = fechaArreglo;
        this.imageArreglo = imageArreglo;
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

    public String getDescripcionEstadoIncidencia() {
        return descripcionEstadoIncidencia;
    }

    public void setDescripcionEstadoIncidencia(String descripcionEstadoIncidencia) {
        this.descripcionEstadoIncidencia = descripcionEstadoIncidencia;
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

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDescripcionArreglo() {
        return descripcionArreglo;
    }

    public void setDescripcionArreglo(String descripcionArreglo) {
        this.descripcionArreglo = descripcionArreglo;
    }

    public String getFechaArreglo() {
        return fechaArreglo;
    }

    public void setFechaArreglo(String fechaArreglo) {
        this.fechaArreglo = fechaArreglo;
    }

    public Bitmap getImageArreglo() {
        return imageArreglo;
    }

    public void setImageArreglo(Bitmap imageArreglo) {
        this.imageArreglo = imageArreglo;
    }
}
