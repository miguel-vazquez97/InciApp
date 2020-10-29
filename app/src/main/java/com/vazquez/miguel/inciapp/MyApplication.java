package com.vazquez.miguel.inciapp;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class MyApplication extends Application {

    private static final String[] codigos={"0||sesionCaducada||"};

    private Socket socket;

    private String correo;
    private boolean conectadoServidor;

    public MyApplication(){
        conectadoServidor=false;
    }

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

    public boolean getConectadoServidor(){
        return conectadoServidor;
    }

    public void setConectadoServidor(boolean conectadoServidor){
        this.conectadoServidor = conectadoServidor;
    }

    public boolean conectarConServidor(String ip, int puerto){
        int timeout = 4000;
        try {
            InetSocketAddress servidorAddr = new InetSocketAddress(ip, puerto);
            Log.i("I/TCP Client", "Connecting...");
            socket = new Socket();
            socket.connect(servidorAddr, timeout);
            Log.i("I/TCP Client", "Connected to server");

            InputStream leerServidor = socket.getInputStream();
            byte[] resServidor = new byte[1024];
            leerServidor.read(resServidor);

            setSocket(socket);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
