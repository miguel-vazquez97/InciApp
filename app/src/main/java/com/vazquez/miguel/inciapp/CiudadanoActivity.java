package com.vazquez.miguel.inciapp;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class CiudadanoActivity extends AppCompatActivity {

    protected MyApplication app;
    int tipoUsuario;
    boolean rolUnico;
    protected boolean cerrarApp=false;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ciudadano);

        app = (MyApplication) getApplication();

        Bundle datos = this.getIntent().getExtras();
        tipoUsuario = datos.getInt("tipo_usuario");
        //con esta variable sabremos si el usuario es unicamente ciudadano
        //o puede ser un usuario con rol supervisor/empleado y que ha decidido entrar en su apartado ciudadano
        rolUnico = datos.getBoolean("rolUnico");

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
        //si no es un usuario con el rol unico de ciudadano, no mostraremos el menu de cerrar sesion
        //para que pueda volver a la pantalla de seleccion de rol
        if(!rolUnico)
            return false;

        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.action_settings_logOut:
                LogOutTask logOutTask = new LogOutTask();
                logOutTask.execute();
                break;
            case R.id.action_settings_modificarUser:
                Intent intent = new Intent(getApplication(), ModificarUsuario.class);
                intent.putExtra("tipo_usuario", tipoUsuario);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if(!rolUnico) {
            super.onBackPressed();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(CiudadanoActivity.this);
            builder.setMessage(getResources().getString(R.string.mensaje_cerrar_app));
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cerrarApp = true;
                    LogOutTask logOutTask = new LogOutTask();
                    logOutTask.execute();
                    finishAffinity();
                    System.exit(0);
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

    }

    class LogOutTask extends AsyncTask<String, Void, Boolean> {
        boolean respuesta;

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            respuesta = app.logOut();
            //si es false y no estamos ya conectado al servidor significa que nuestra sesion ha caducado
            if(!respuesta && !app.getConectadoServidor())
                cancel(true);

            return respuesta;
        }

        @Override
        protected void onPostExecute(Boolean value){

            if(value){
                Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
                app.setCorreo(null);

                if(!cerrarApp){
                    Intent intent = new Intent(CiudadanoActivity.this, MainActivity.class);
                    //borramos la pila de activity anteriores
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CiudadanoActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

}
