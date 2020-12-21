package com.vazquez.miguel.inciapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ListadoIncidencias extends AppCompatActivity {

    protected MyApplication app;
    private Socket socket;

    protected InputStream leerServidor;
    protected OutputStream enviarServidor;

    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutManager;
    protected IncidenciaAdapter adapter;

    protected String tipo_incidencia;
    protected int tipoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_historial_incidencias);

        Bundle datos = this.getIntent().getExtras();
        tipo_incidencia = datos.getString("tipo_incidencia");
        tipoUsuario = datos.getInt("tipo_usuario");

        switch(tipo_incidencia){
            case "incidencias_historial":
                getSupportActionBar().setTitle("Historial Incidencias");
                break;
            case "incidencias_activas":
                getSupportActionBar().setTitle("Incidencias Activas");
                break;
            case "incidencias_enTramite":
                getSupportActionBar().setTitle("Incidencias en Tramite");
                break;
            case "incidencias_enArreglo":
                getSupportActionBar().setTitle("Incidencias en Arreglo");
                break;
            case "incidencias_validarArreglo":
                getSupportActionBar().setTitle("Validar Arreglo de Incidencias");
                break;
        }

        app = (MyApplication) getApplication();
        socket = app.getSocket();

        try {
            enviarServidor = socket.getOutputStream();
            leerServidor = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.reciclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        HistorialIncidenciasTask registrarUsuarioTask = new HistorialIncidenciasTask();
        registrarUsuarioTask.execute();


    }


    class HistorialIncidenciasTask extends AsyncTask<Void, Void, ArrayList<IncidenciaRow>> {

        ProgressBar progressBar;
        String envioServidor;
        String respuestaServidor;
        byte[] respuestaSer;
        String[] resServidor;

        @Override
        protected void onPreExecute(){
            progressBar = findViewById(R.id.progressbar_historial);
        }

        @Override
        protected ArrayList<IncidenciaRow> doInBackground(Void... voids) {

            switch(tipoUsuario){

                case 2:
                    if(tipo_incidencia.equals("incidencias_enTramite")){
                        envioServidor = "56||"+app.getCorreo()+"||enTramite||";
                    }else{
                        envioServidor = "57||"+app.getCorreo()+"||validarArreglo||";
                    }
                    break;

                case 3:
                    envioServidor = "58||"+app.getCorreo()+"||enArreglo||";;
                    break;

                case 4:
                    if(tipo_incidencia.equals("incidencias_historial")){
                        envioServidor = "54||"+app.getCorreo()+"||historial||";
                    }else{
                        envioServidor = "55||"+app.getCorreo()+"||activas||";
                    }
                    break;
            }



            ArrayList<IncidenciaRow> arrayIncidencias = new ArrayList<>();
            try{
                while(leerServidor.available()>0){
                    leerServidor.read(respuestaSer = new byte[leerServidor.available()]);
                }

                byte[] envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

                int time=0;
                while(leerServidor.available()<1 && time<10000){
                    try {
                        Thread.sleep(500);
                        time += 500;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(time==10000){
                    return null;
                }

                respuestaSer = new byte[leerServidor.available()];
                leerServidor.read(respuestaSer);
                respuestaServidor = new String(respuestaSer);
                resServidor = respuestaServidor.split("\\|\\|");

                if(resServidor[0].equals("59") && resServidor[1].equals("listadoIncidenciasOk")){

                    String data = resServidor[2];
                    JSONArray jsonArray = new JSONArray(data);

                    for( int i=0; i<jsonArray.length(); i++){
                        JSONObject obj = jsonArray.getJSONObject(i);
                        IncidenciaRow incidencia = new IncidenciaRow(obj.getInt("id"),obj.getString("tipo"),obj.getString("estado"),obj.getString("ubicacion"),obj.getString("direccion"));
                        arrayIncidencias.add(incidencia);
                    }

                }

            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
            }catch(JSONException jsonex){
                jsonex.printStackTrace();
            }

            return arrayIncidencias;

        }

        @Override
        protected void onPostExecute(ArrayList<IncidenciaRow> arrayIncidencias) {
            progressBar.setVisibility(View.GONE);

            if(arrayIncidencias==null){
                Toast.makeText(getApplication(), getApplication().getString(R.string.mensaje_error_conection_server), Toast.LENGTH_LONG).show();
                return;
            }

            adapter = new IncidenciaAdapter(arrayIncidencias, R.layout.item_row_historial, ListadoIncidencias.this, tipoUsuario, app, new IncidenciaAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(IncidenciaRow incidencia, int position) {
                    switch(tipoUsuario){
                        case 2:
                            Intent intent2 = new Intent (getApplication(), SupervisorIncidencia.class);
                            intent2.putExtra("id_incidencia", incidencia.getId());
                            intent2.putExtra("tipo_usuario", tipoUsuario);
                            if(tipo_incidencia.equals("incidencias_enTramite")){
                                intent2.putExtra("tipo_estado","enTramite");
                            }else{
                                intent2.putExtra("tipo_estado","validarArreglo");
                            }
                            startActivity(intent2);
                            finish();
                            break;

                        case 3:
                            Intent intent3 = new Intent (getApplication(), EmpleadoActivity.class);
                            intent3.putExtra("id_incidencia", incidencia.getId());
                            startActivity(intent3);
                            finish();
                            break;

                        case 4:
                            Intent intent = new Intent(getApplication(), DetallesIncidencia.class);
                            intent.putExtra("id_incidencia", incidencia.getId());
                            startActivity(intent);
                            break;
                    }

                }
            });
            recyclerView.setAdapter(adapter);

        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ListadoIncidencias.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
