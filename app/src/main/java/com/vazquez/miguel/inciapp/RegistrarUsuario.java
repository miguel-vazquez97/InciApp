package com.vazquez.miguel.inciapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrarUsuario extends AppCompatActivity {

    protected MyApplication app;
    private Socket socket;

    protected InputStream leerServidor;
    protected OutputStream enviarServidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_registrar_usuario);

        getSupportActionBar().setTitle("Registrarse");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        app = (MyApplication) getApplication();
        socket = app.getSocket();

        try {
            enviarServidor = socket.getOutputStream();
            leerServidor = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button boton_aceptar = findViewById(R.id.boton_acept);
        boton_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

    }

    protected boolean registrarUsuario(){

        boolean registrar_usuario = true;

        TextView text_correo = findViewById(R.id.correo_text_reg);
        String correo =  String.valueOf(text_correo.getText());

        if(correo.isEmpty()) {
            text_correo.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_correo.setTextColor(Color.WHITE);
            registrar_usuario = false;
        }else{
            if(!comprobarCorreo(correo)){
                text_correo.setBackgroundColor(Color.rgb( 228, 83, 94));
                text_correo.setTextColor(Color.WHITE);
                registrar_usuario = false;
            }else{
                text_correo.setBackgroundColor(Color.rgb( 221, 222, 222));
                text_correo.setTextColor(Color.rgb(123, 164, 168));
            }
        }


        TextView text_contra = findViewById(R.id.contra_text_reg);
        String contra = String.valueOf(text_contra.getText());

        if(contra.isEmpty()){
            text_contra.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_contra.setTextColor(Color.WHITE);
            registrar_usuario = false;
        }else{
            text_contra.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_contra.setTextColor(Color.rgb(123, 164, 168));
        }

        TextView text_contra_rep = findViewById(R.id.repcontra_text_reg);
        String contra_rep = String.valueOf(text_contra_rep.getText());

        if(contra_rep.isEmpty()){
            text_contra_rep.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_contra_rep.setTextColor(Color.WHITE);
            registrar_usuario = false;
        }else{
            if(!contra.equals(contra_rep)){
                text_contra_rep.setBackgroundColor(Color.rgb( 228, 83, 94));
                text_contra_rep.setTextColor(Color.WHITE);
                registrar_usuario = false;
            }else{
                text_contra_rep.setBackgroundColor(Color.rgb( 221, 222, 222));
                text_contra_rep.setTextColor(Color.rgb(123, 164, 168));
            }
        }

        TextView text_nombre = findViewById(R.id.nombre_text_reg);
        String nombre = String.valueOf(text_nombre.getText());

        if(nombre.isEmpty()){
            text_nombre.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_nombre.setTextColor(Color.WHITE);
            registrar_usuario = false;
        }else{
            text_nombre.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_nombre.setTextColor(Color.rgb(123, 164, 168));
        }

        TextView text_apellidos = findViewById(R.id.apellidos_text_reg);
        String apellidos = String.valueOf(text_apellidos.getText());

        if(apellidos.isEmpty()){
            text_apellidos.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_apellidos.setTextColor(Color.WHITE);
            registrar_usuario = false;
        }else{
            text_apellidos.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_apellidos.setTextColor(Color.rgb(123, 164, 168));
        }

        TextView text_dni = findViewById(R.id.dni_text_reg);
        String dni = String.valueOf(text_dni.getText());

        if(dni.isEmpty()){
            text_dni.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_dni.setTextColor(Color.WHITE);
            registrar_usuario = false;
        }else{
            text_dni.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_dni.setTextColor(Color.rgb(123, 164, 168));
        }

        TextView text_tlf = findViewById(R.id.tlf_text_reg);
        String tlf = String.valueOf(text_tlf.getText());

        if(tlf.isEmpty() || tlf.length()<9){
            text_tlf.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_tlf.setTextColor(Color.WHITE);
            registrar_usuario = false;
        }else{
            text_tlf.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_tlf.setTextColor(Color.rgb(123, 164, 168));
        }

        if(registrar_usuario) {
            RegistrarUsuarioTask registrarUsuarioTask = new RegistrarUsuarioTask();
            registrarUsuarioTask.execute(correo,contra,nombre,apellidos,dni,tlf);
        }

        return false;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    class RegistrarUsuarioTask extends AsyncTask<String, Void, Boolean> {

        ProgressBar progressBar;
        Button boton_aceptar;
        String envioServidor;
        String respuestaServidor;
        String[] resServidor;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            boton_aceptar = findViewById(R.id.boton_acept);
            boton_aceptar.setVisibility(View.GONE);
            progressBar = findViewById(R.id.progressbar_reg);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            envioServidor = "51||"+strings[0]+"||"+strings[1]+"||"+strings[2]+"||"+strings[3]+"||"+strings[4]+"||"+strings[5]+"||";

            try{
                byte[] envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();
                byte[] respuestaSer = new byte[1024];
                leerServidor.read(respuestaSer);
                respuestaServidor = new String(respuestaSer);

                resServidor = respuestaServidor.split("\\|\\|");

                if(resServidor[0].equals("50") && resServidor[1].equals("registrarUsuarioOk")){
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return false;

        }

        @Override
        protected void onPostExecute(Boolean value){
            progressBar.setVisibility(View.GONE);
            boton_aceptar.setVisibility(View.VISIBLE);

            if(!value){
                Toast.makeText(getApplication(),getApplication().getString(R.string.regis_no),Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplication(),getApplication().getString(R.string.regis_ok),Toast.LENGTH_LONG).show();

                finish();
            }
        }
    }
}

