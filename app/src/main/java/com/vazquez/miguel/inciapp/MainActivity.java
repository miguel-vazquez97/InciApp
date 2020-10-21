package com.vazquez.miguel.inciapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private static String direccion_ip = null;
    private static String puerto = null;
    private static boolean conectadoServidor = false;

    private Socket socket;
    protected InputStream input;
    protected OutputStream enviarServidor;

    protected MyApplication app;
    protected SharedPreferences preferences;
    protected SharedPreferences.Editor editor;

    protected View view;
    protected AlertDialog.Builder builder;
    protected Dialog dialog;

    protected TextView text_correo;
    protected  TextView text_contra;

    protected String sesionIniciada;
    protected ConectarServidorTask conexionServidor;

    protected boolean popup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        app = (MyApplication)getApplication();

        //accedemos al archivo de propiedades
        preferences = getSharedPreferences("propiedades", Context.MODE_PRIVATE);
        editor = preferences.edit();

        direccion_ip = preferences.getString("direccionIP", "");
        puerto = preferences.getString("puerto", "");

        //conectamos al servidor si ya tenemos la ip y puerto introducidos anteriormente
        if(!conectadoServidor && direccion_ip.length() > 0 && puerto.length() > 0) {
            //mostraremos un mensaje inicial mientras nos conectamos al servidor
            builder = new AlertDialog.Builder(this).setMessage("Conectando al servidor...");
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();

            conexionServidor = new ConectarServidorTask();
            conexionServidor.execute();
        }

        text_correo = findViewById(R.id.correo_log);
        String usu_correo = preferences.getString("correo_usuario","");
        text_correo.setText(usu_correo);

        text_contra = findViewById(R.id.contra_log);
        String usu_contra = preferences.getString("contra_usuario","");
        text_contra.setText(usu_contra);
        //con esta variable sabremos si ya habiamos iniciado sesion en este dispositivo
        //con el usuario y contraseña introducidos previamente
        sesionIniciada = preferences.getString("sesion.iniciada", "");

        Button boton_log = (Button) findViewById(R.id.boton_loggin);
        boton_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conectadoServidor){
                    comprobarLoggin();
                }else{
                    Toast.makeText(getApplication(),getApplication().getString(R.string.servidor_no),Toast.LENGTH_LONG).show();
                }
            }
        });

        Button boton_registro = (Button) findViewById(R.id.boton_registrar);
        boton_registro.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(conectadoServidor) {
                    text_correo.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                    text_correo.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));

                    text_correo.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                    text_correo.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));

                    //Intent intent = new Intent(v.getContext(), RegistrarUsuario.class);
                    //startActivity(intent);
                }else{
                    Toast.makeText(getApplication(),getApplication().getString(R.string.servidor_no),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        if (item.getItemId() == R.id.action_settings) {
            if (conectadoServidor) {
                Toast.makeText(getApplication(), getApplication().getString(R.string.ok_servidor), Toast.LENGTH_LONG).show();
            } else {
                popup = true;
                createPopUp();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void createPopUp(){
        builder = new AlertDialog.Builder(this);
        view = LayoutInflater.from(this).inflate(R.layout.popup,null);

        builder.setView(view);

        dialog = builder.create();
        dialog.show();


        final EditText ip_servidor = view.findViewById(R.id.popup_ip);
        ip_servidor.setText(direccion_ip);

        final EditText puerto_servidor = view.findViewById(R.id.port_servidor);
        puerto_servidor.setText(puerto);

        Button boton_aceptar = view.findViewById(R.id.popup_boton);

        boton_aceptar.setOnClickListener(v -> {

            if(!TextUtils.isEmpty(ip_servidor.getText()) && !TextUtils.isEmpty(puerto_servidor.getText())){

                direccion_ip = String.valueOf(ip_servidor.getText());
                puerto = String.valueOf(puerto_servidor.getText());

                editor.putString("direccionIP",direccion_ip);
                editor.putString("puerto",puerto);
                editor.commit();

                conexionServidor = new ConectarServidorTask();
                conexionServidor.execute();

            }else{
                Toast.makeText(getApplication(),getApplication().getString(R.string.error_popup),Toast.LENGTH_LONG).show();
            }
        });


    }

    protected void comprobarLoggin(){
        boolean valido = true;


        String correo = String.valueOf(text_correo.getText());

        if(correo.isEmpty()){
            text_correo.setBackgroundColor(ContextCompat.getColor(this, R.color.colorError));
            text_correo.setTextColor(Color.WHITE);
            valido = false;
        }else{
            if(!comprobarCorreo(correo)){
                text_correo.setBackgroundColor(ContextCompat.getColor(this, R.color.colorError));
                text_correo.setTextColor(Color.WHITE);
                valido = false;
            }else{
                text_correo.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSecond));
                text_correo.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            }
        }


        String contra = String.valueOf(text_contra.getText());

        if(contra.isEmpty()){
            text_contra.setBackgroundColor(ContextCompat.getColor(this, R.color.colorError));
            text_contra.setTextColor(Color.WHITE);
            valido = false;
        }else{
            text_contra.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSecond));
            text_contra.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        if(valido){
            editor.putString("correo_usuario",correo);
            editor.putString("contra_usuario",contra);
            editor.commit();
            LoggearUsuarioTask loggearUsuario = new LoggearUsuarioTask();
            loggearUsuario.execute(correo,contra);
        }
    }

    public boolean comprobarCorreo(String correo){
        int n1 = correo.indexOf("@");
        int n2 = correo.indexOf(".");


        if(n1<0 || n2 <0 )
            return false;


        if(correo.substring(n2).equals(".es") || correo.substring(n2).equals(".com"))
            return true;


        return false;
    }



    class ConectarServidorTask extends AsyncTask<String, Void, Boolean> {

        ProgressBar progressBar;
        Button boton_aceptar;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //comprobamos si el popup es false, no lo creamos
            //popup será false solo al iniciar la app cuando intentemos conectarnos de manera automática al servidor
            if(popup){
                boton_aceptar = view.findViewById(R.id.popup_boton);
                boton_aceptar.setVisibility(View.GONE);
                progressBar = view.findViewById(R.id.progressbar_popup);
                progressBar.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            //limitaremos a 4s de espera para conectarse al servidor
            int timeout = 4000;
            try {
                InetSocketAddress servidorAddr = new InetSocketAddress(direccion_ip, Integer.parseInt(puerto));
                Log.i("I/TCP Client", "Connecting...");
                socket = new Socket();
                socket.connect(servidorAddr, timeout);
                Log.i("I/TCP Client", "Connected to server");

                input = socket.getInputStream();
                byte[] resServidor = new byte[1024];
                enviarServidor = socket.getOutputStream();
                input.read(resServidor);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;

        }

        @Override
        protected void onPostExecute(Boolean value){
            if(popup) {
                progressBar.setVisibility(View.GONE);
                boton_aceptar.setVisibility(View.VISIBLE);
            }
            if(value){
                dialog.dismiss();
                app.setSocket(socket);
                conectadoServidor = true;
                Toast.makeText(getApplication(),getApplication().getString(R.string.ok_servidor),Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplication(),getApplication().getString(R.string.fail_servidor),Toast.LENGTH_LONG).show();
                //si el dialog abierto no es el popup, si no el dialog que aparece cuando iniciamos la app, haremos dismiss() para dejar de mostrarlo
                if(!popup)
                    dialog.dismiss();
            }
        }
    }

    class LoggearUsuarioTask extends AsyncTask<String, Void, String> {

        ProgressBar progressBar;
        Button boton_log;

        String envioServidor;
        String respuestaServidor;
        String[] resServidor;

        int tipoUsuario;
        String correo, respuesta;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar = findViewById(R.id.progressbar_activitymain);
            progressBar.setVisibility(View.VISIBLE);
            boton_log = findViewById(R.id.boton_loggin);
            boton_log.setVisibility(View.GONE);

            respuesta = "";
        }

        @Override
        protected String doInBackground(String... strings) {
            envioServidor = "52||"+strings[0]+"||"+strings[1]+"||"+sesionIniciada+"||";

            try{
                byte[] envioSer = envioServidor.getBytes();
                //enviarServidor.println(envioServidor);
                enviarServidor.write(envioSer);
                enviarServidor.flush();
                //respuestaServidor = leerServidor.readLine();
                byte[] respuestaSer = new byte[input.available()];
                input.read(respuestaSer);
                respuestaServidor = new String(respuestaSer);
                resServidor = respuestaServidor.split("\\|\\|");

                if(resServidor[0].equals("53") && resServidor[1].equals("logUsuarioOk")){
                    correo = resServidor[2];
                    tipoUsuario = Integer.parseInt(resServidor[3]);
                }

                respuesta = resServidor[1];

            } catch (IOException e){
                e.printStackTrace();
            }

            return respuesta;

        }

        @Override
        protected void onPostExecute(String value){
            progressBar.setVisibility(View.GONE);
            boton_log.setVisibility(View.VISIBLE);

            if(respuesta.equals("logUsuarioOk")){
                //registramos el correo en nuestra clase MyApplication
                app.setCorreo(correo);
                //registraremos en nuestro archivo que hemos iniciado sesion en este archivo
                //si no lo habiamos hecho previamente con este correo y contreaseña
                if(sesionIniciada.equals("false")) {
                    editor.putString("sesion.iniciada", "true");
                    editor.commit();
                }

                //si el usuario tiene rol = 2(supervisor) o rol = 3(empleador)
                //podremos elegir entrar a la app como supervisor/empleado o ciudadano
                /*
                if(tipoUsuario==2 || tipoUsuario==3) {
                    Intent intent = new Intent(getApplication(), SelectRol.class);
                    intent.putExtra("tipo_usuario", tipoUsuario);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplication(), CiudadanoActivity.class);
                    intent.putExtra("tipo_usuario", tipoUsuario);
                    startActivity(intent);
                }   */
                //Intent intent = new Intent(getApplication(), CiudadanoActivity.class);
                //intent.putExtra("tipo_usuario", tipoUsuario);
                //startActivity(intent);
            }else{
                Toast.makeText(getApplication(),getApplication().getString(R.string.log_no),Toast.LENGTH_LONG).show();
            }
        }
    }
}