package com.vazquez.miguel.inciapp;

public class Usuario implements Comparable<Usuario>{

    private String correo;
    private String contrasena;
    private String nombre;
    private String apellido;
    private String dni;
    private int tlf;
    private String departamento;

    public Usuario(String correo, String contrasena, String nombre, String apellido, String dni, int tlf, String departamento) {
        this.correo = correo;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.tlf = tlf;
        this.departamento = departamento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public int getTlf() {
        return tlf;
    }

    public void setTlf(int tlf) {
        this.tlf = tlf;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    @Override
    public int compareTo(Usuario u) {
        int estado=-1;
        if(this.contrasena.equals(u.getContrasena()) && this.nombre.equals(u.getNombre()) && this.apellido.equals(u.getApellido()) && this.dni.equals(u.getDni()) && this.tlf==u.getTlf()){
            //los objetos son iguales
            estado=0;
        }else{
            estado=1;
        }
        return estado;
    }
}

