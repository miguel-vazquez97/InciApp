package com.vazquez.miguel.inciapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class CiudadanoActivity extends AppCompatActivity {

    protected MyApplication app;
    private Socket socket;
    protected InputStream recibirServidor;
    protected OutputStream enviarServidor;
    int tipoUsuario;
    protected boolean cerrarApp=false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ciudadano);

        app = (MyApplication) getApplication();
        socket = app.getSocket();

        Bundle datos = this.getIntent().getExtras();
        tipoUsuario = datos.getInt("tipo_usuario");

        Button boton_nuevaInci = findViewById(R.id.boton_nueva_incidencia);
        boton_nuevaInci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), NuevaIncidencia.class);
                startActivity(intent);
            }
        });

        Button boton_inciActivas = findViewById(R.id.boton_activas);
        boton_inciActivas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ListadoIncidencias.class);
                intent.putExtra("tipo_incidencia", "incidencias_activas");
                intent.putExtra("tipo_usuario", tipoUsuario);
                startActivity(intent);
            }
        });

        Button boton_historialInci = findViewById(R.id.boton_historial);
        boton_historialInci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ListadoIncidencias.class);
                intent.putExtra("tipo_incidencia", "incidencias_historial");
                intent.putExtra("tipo_usuario", tipoUsuario);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_log_out, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        if (item.getItemId() == R.id.action_settings_logOut) {
            LogOutTask logOutTask = new LogOutTask();
            logOutTask.execute();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CiudadanoActivity.this);
        builder.setMessage(getResources().getString(R.string.mensaje_cerrar_app));
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cerrarApp=true;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class LogOutTask extends AsyncTask<String, Void, Boolean> {

        String envioServidor;
        String respuestaServidor;
        String[] resServidor;
        boolean respuesta;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            respuesta = false;
            socket = app.getSocket();
            try {
                recibirServidor = socket.getInputStream();
                enviarServidor = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            envioServidor = "65||logOutUsuario||";

            try{
                byte[] envioSer = envioServidor.getBytes();
                //enviarServidor.println(envioServidor);
                enviarServidor.write(envioSer);
                enviarServidor.flush();
                //respuestaServidor = leerServidor.readLine();
                byte[] respuestaSer = new byte[1024];
                recibirServidor.read(respuestaSer);
                respuestaServidor = new String(respuestaSer);
                resServidor = respuestaServidor.split("\\|\\|");

                if((resServidor[0].equals("70") && resServidor[1].equals("logOutOk"))
                        || (resServidor[0].equals("71") && resServidor[1].equals("salirAppOk"))){
                    respuesta = true;
                }

            } catch (IOException e){
                e.printStackTrace();
            }

            return respuesta;

        }

        @Override
        protected void onPostExecute(Boolean value){

            if(value){
                Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
                app.setCorreo(null);

                SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("sesion.iniciada","false");
                editor.apply();

                //app.setConectadoServidor(false);
                //Intent intent = new Intent(CiudadanoActivity.this, MainActivity.class);
                //startActivity(intent);
                finish();
            }
        }
    }

}
