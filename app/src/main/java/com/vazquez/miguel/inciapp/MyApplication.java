package com.vazquez.miguel.inciapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

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
            OutputStream escribirServidor = socket.getOutputStream();
            escribirServidor.write("ConectadoAppMovil||1||".getBytes());
            escribirServidor.flush();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean logOut(){
        boolean respuesta = false;
        byte[] respuestaSer;

        try {
            InputStream recibirServidor = socket.getInputStream();
            OutputStream enviarServidor = socket.getOutputStream();

            if(recibirServidor.available()>0){
                recibirServidor.read(respuestaSer = new byte[recibirServidor.available()]);
            }

            byte[] envioSer = "65||logOutUsuario||".getBytes();
            enviarServidor.write(envioSer);
            enviarServidor.flush();

            while(recibirServidor.available()<1){}

            respuestaSer = new byte[recibirServidor.available()];
            recibirServidor.read(respuestaSer);
            String respuestaServidor = new String(respuestaSer);
            String[] resServidor = respuestaServidor.split("\\|\\|");

            if(resServidor[0].equals("72") && resServidor[1].equals("logOutOk")){
                respuesta=true;

                SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("sesion.iniciada","false");
                editor.apply();

                cerrarSesion();
            }

        } catch (IOException e) {
            e.printStackTrace();
            cerrarSesion();
            return false;
        }

        return respuesta;
    }

    public void cerrarSesion(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        conectadoServidor=false;

    }

}
