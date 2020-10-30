package com.vazquez.miguel.inciapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

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
        String[] resServidor;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar = findViewById(R.id.progressbar_historial);
        }

        @Override
        protected ArrayList<IncidenciaRow> doInBackground(Void... voids) {

            if (tipoUsuario == 4) {
                if (tipo_incidencia.equals("incidencias_historial")) {
                    envioServidor = "54||" + app.getCorreo() + "||historial||";
                } else {
                    envioServidor = "55||" + app.getCorreo() + "||activas||";
                }
            }



            ArrayList<IncidenciaRow> arrayIncidencias = new ArrayList<>();
            try{
                byte[] envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();
                while(leerServidor.available()<1){}
                byte[] respuestaSer = new byte[leerServidor.available()];
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

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return arrayIncidencias;

        }

        @Override
        protected void onPostExecute(ArrayList<IncidenciaRow> arrayIncidencias) {
            progressBar.setVisibility(View.GONE);

            adapter = new IncidenciaAdapter(arrayIncidencias, R.layout.item_row_historial, ListadoIncidencias.this, tipoUsuario, app, new IncidenciaAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(IncidenciaRow incidencia, int position) {
                    switch(tipoUsuario){
                        case 4:
                            Intent intent = new Intent(getApplication(), DetallesIncidencia.class);
                            intent.putExtra("id_incidencia", incidencia.getId());
                            startActivity(intent);
                            break;
                        case 2:

                            break;
                        case 3:

                            break;
                    }

                }
            });
            recyclerView.setAdapter(adapter);

        }
    }
}
