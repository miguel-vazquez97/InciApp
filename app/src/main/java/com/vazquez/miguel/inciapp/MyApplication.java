package com.vazquez.miguel.inciapp;

import android.app.Application;

import java.net.Socket;

public class MyApplication extends Application {

    private Socket socket;
    private String correo;

    public Socket getSocket(){
        return socket;
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }

    public String getCorreo(){
        return correo;
    }

    public void setCorreo(String correo){
        this.correo = correo;
    }
}
