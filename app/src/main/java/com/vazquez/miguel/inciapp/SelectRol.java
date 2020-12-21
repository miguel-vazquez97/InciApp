package com.vazquez.miguel.inciapp;

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

public class SelectRol extends AppCompatActivity {

    protected MyApplication app;
    protected int tipoUsuario;
    protected boolean cerrarApp=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_select_rol);

        getSupportActionBar().setTitle("Acceder como...");

        app = (MyApplication)getApplication();

        Bundle datos = this.getIntent().getExtras();
        tipoUsuario = datos.getInt("tipo_usuario");

        Button boton_ciudadano = findViewById(R.id.boton_ciudadano);
        boton_ciudadano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), CiudadanoActivity.class);
                intent.putExtra("tipo_usuario", 4);
                intent.putExtra("rolUnico",false);
                startActivity(intent);
            }
        });

        Button boton_supervisor = findViewById(R.id.boton_supervisor);
        boton_supervisor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SupervisorActivity.class);
                intent.putExtra("tipo_usuario", 2);
                startActivity(intent);
            }
        });

        Button boton_empleado = findViewById(R.id.boton_empleado);
        boton_empleado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ListadoIncidencias.class);
                intent.putExtra("tipo_incidencia", "incidencias_enArreglo");
                intent.putExtra("tipo_usuario", 3);
                startActivity(intent);
            }
        });

        if(tipoUsuario == 2){
            boton_supervisor.setVisibility(View.VISIBLE);
            boton_empleado.setVisibility(View.GONE);
        }else if(tipoUsuario == 3){
            boton_supervisor.setVisibility(View.GONE);
            boton_empleado.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectRol.this);
        builder.setMessage(getResources().getString(R.string.mensaje_cerrar_app));
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cerrarApp=true;
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
        //ejecuta super.onBackPressed() para que finalice el metodo cerrando el activity
        //super.onBackPressed();
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
                    Intent intent = new Intent(SelectRol.this, MainActivity.class);
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
            Intent intent = new Intent(SelectRol.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
