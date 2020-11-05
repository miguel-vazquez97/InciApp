package com.vazquez.miguel.inciapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class DetallesIncidencia extends AppCompatActivity {

    protected MyApplication app;
    private Socket socket;

    protected InputStream leerServidor;
    protected OutputStream enviarServidor;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;

    int id_incidencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detalle_incidencia);
        getSupportActionBar().setTitle(getResources().getString(R.string.DetalleIncidencia));

        Bundle datos = this.getIntent().getExtras();
        id_incidencia = datos.getInt("id_incidencia");


        app = (MyApplication) getApplication();
        socket = app.getSocket();

        try {
            enviarServidor = socket.getOutputStream();
            leerServidor = socket.getInputStream();

            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        DetalleIncidenciaTask detalleIncidenciaTask = new DetalleIncidenciaTask();
        detalleIncidenciaTask.execute();

    }

    class DetalleIncidenciaTask extends AsyncTask<Void, Void, ArrayList<IncidenciaDetalle>> {

        ProgressBar progressBar;
        String envioServidor;
        byte[] envioSer;
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar = findViewById(R.id.progressbar_detalle_incidencia);
        }

        @Override
        protected ArrayList<IncidenciaDetalle> doInBackground(Void... voids) {
            envioServidor = "60||" + id_incidencia + "||";
            ArrayList<IncidenciaDetalle> arrayIncidencias = new ArrayList<>();
            try {
                envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

                //tamano que tendra nuestra cadena en base64
                int size = inputStream.readInt();

                while(inputStream.available()<1){}

                byte[] arrayBytesBase64;
                String base64="", cadena;
                while(base64.length()<size){
                    arrayBytesBase64 = new byte[inputStream.available()];
                    //recibimos bytes
                    inputStream.read(arrayBytesBase64);
                    //parseamos a cadena
                    cadena = new String(arrayBytesBase64);
                    //unimos a la cadena que formara nuestro Base64
                    base64 += cadena;
                }

                //descodificamos a bytes
                byte[] arrayBytes = Base64.decode(base64, Base64.DEFAULT);
                //creamos lo que será nuestro JSONArray en cadena
                String cadena_detalles_incidencia = new String(arrayBytes);
                //creamos el JSONArray a partir de la cadena anterior
                JSONArray jsonArray = new JSONArray(cadena_detalles_incidencia);

                LocalDate localDate;
                String fecha, imagenBase64;
                String[] amd;
                JSONObject obj;
                byte[] byteArray;
                Bitmap bmpImage;

                for (int i = 0; i < jsonArray.length(); i++) {
                    obj = jsonArray.getJSONObject(i);

                    fecha = obj.getString("fecha");
                    amd = fecha.split("-");
                    localDate = LocalDate.of(Integer.parseInt(amd[0]), Integer.parseInt(amd[1]), Integer.parseInt(amd[2]));

                    if(obj.has("imagen")){
                        if(!obj.getString("imagen").isEmpty()){
                            imagenBase64 = obj.getString("imagen");
                            byteArray = Base64.decode(imagenBase64, Base64.DEFAULT);
                            bmpImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        }else{
                            bmpImage = null;
                        }
                    }else{
                        bmpImage = null;
                    }

                    IncidenciaDetalle incidenciaDetalle = new IncidenciaDetalle(obj.getString("estado"), obj.getString("ubicacion"), obj.getString("direccion"), obj.getString("descripcion"), obj.getString("tipo"), localDate, obj.getString("descripcionEstadoIncidencia"), bmpImage);
                    arrayIncidencias.add(incidenciaDetalle);
                }

            } catch (JSONException | IOException ex) {
                ex.printStackTrace();
            }

            return arrayIncidencias;
        }

        @Override
        protected void onPostExecute(ArrayList<IncidenciaDetalle> arrayIncidencias) {
            progressBar.setVisibility(View.GONE);

            LinearLayout linear_NR, linear_ET, linear_V, linear_EA, linear_A, linear_S, linear_D;
            TextView estado_NR, tipo_NR, fecha_NR, descripcion_NR, estado_ET, fecha_ET, estado_V, fecha_V, estado_EA, fecha_EA, estado_A, fecha_A, estado_S, fecha_S, descripcion_S, estado_D, fecha_D, descripcion_D;
            ImageView imagen_NR, imagen_S;
            View separador_NR, separador_ET, separador_V, separador_EA, separador_A;
            for(IncidenciaDetalle incidencia : arrayIncidencias){
                switch(incidencia.getEstado()){
                    case "NuevaRegistrada":
                        linear_NR = findViewById(R.id.linear_nueva_registrada);
                        linear_NR.setVisibility(View.VISIBLE);
                        estado_NR = findViewById(R.id.estadoNR);
                        estado_NR.setText(getResources().getString(R.string.NuevaRegistrada));
                        tipo_NR = findViewById(R.id.tipoNR);
                        tipo_NR.setText(incidencia.getTipo());
                        fecha_NR = findViewById(R.id.fechaNR);
                        fecha_NR.setText(incidencia.getDate().toString());
                        imagen_NR = findViewById(R.id.imagenNR);
                        imagen_NR.setImageBitmap(incidencia.getImage());
                        descripcion_NR = findViewById(R.id.descripcionNR);
                        descripcion_NR.setText(incidencia.getDescripcion());
                        break;

                    case "EnTramite":
                        separador_NR = findViewById(R.id.separadorNR);
                        separador_NR.setVisibility(View.VISIBLE);

                        linear_ET = findViewById(R.id.linear_en_tramite);
                        linear_ET.setVisibility(View.VISIBLE);
                        estado_ET = findViewById(R.id.estadoET);
                        estado_ET.setText(getResources().getString(R.string.EnTramite));
                        fecha_ET = findViewById(R.id.fechaET);
                        fecha_ET.setText(incidencia.getDate().toString());
                        break;

                    case "Validada":
                        separador_ET = findViewById(R.id.separadorET);
                        separador_ET.setVisibility(View.VISIBLE);

                        linear_V = findViewById(R.id.linear_validada);
                        linear_V.setVisibility(View.VISIBLE);
                        estado_V = findViewById(R.id.estadoV);
                        estado_V.setText(getResources().getString(R.string.Validada));
                        fecha_V = findViewById(R.id.fechaV);
                        fecha_V.setText(incidencia.getDate().toString());
                        break;

                    case "EnArreglo":
                        separador_V = findViewById(R.id.separadorV);
                        separador_V.setVisibility(View.VISIBLE);

                        linear_EA = findViewById(R.id.linear_en_arreglo);
                        linear_EA.setVisibility(View.VISIBLE);
                        estado_EA = findViewById(R.id.estadoEA);
                        estado_EA.setText(getResources().getString(R.string.EnArreglo));
                        fecha_EA = findViewById(R.id.fechaEA);
                        fecha_EA.setText(incidencia.getDate().toString());
                        break;

                    case "Arreglada":
                        separador_EA = findViewById(R.id.separadorEA);
                        separador_EA.setVisibility(View.VISIBLE);

                        linear_A = findViewById(R.id.linear_arreglada);
                        linear_A.setVisibility(View.VISIBLE);
                        estado_A = findViewById(R.id.estadoA);
                        estado_A.setText(getResources().getString(R.string.Arreglada));
                        fecha_A = findViewById(R.id.fechaA);
                        fecha_A.setText(incidencia.getDate().toString());
                        break;

                    case "Solucionada":
                        separador_A = findViewById(R.id.separadorA);
                        separador_A.setVisibility(View.VISIBLE);

                        linear_S = findViewById(R.id.linear_solucionada);
                        linear_S.setVisibility(View.VISIBLE);
                        estado_S = findViewById(R.id.estadoS);
                        estado_S.setText(getResources().getString(R.string.Solucionada));
                        fecha_S = findViewById(R.id.fechaS);
                        fecha_S.setText(incidencia.getDate().toString());
                        imagen_S = findViewById(R.id.imagenS);
                        imagen_S.setImageBitmap(incidencia.getImage());
                        descripcion_S = findViewById(R.id.descripcionS);
                        descripcion_S.setText(incidencia.getDescripcionEstadoIncidencia());
                        break;

                    case "Denegada":
                        separador_ET = findViewById(R.id.separadorET);
                        separador_ET.setVisibility(View.VISIBLE);

                        linear_D = findViewById(R.id.linear_denegado);
                        linear_D.setVisibility(View.VISIBLE);
                        estado_D = findViewById(R.id.estadoD);
                        estado_D.setText(getResources().getString(R.string.Denegada));
                        fecha_D = findViewById(R.id.fechaD);
                        fecha_D.setText(incidencia.getDate().toString());
                        descripcion_D = findViewById(R.id.descripcionD);
                        descripcion_D.setText(incidencia.getDescripcionEstadoIncidencia());
                        break;

                }
            }
        }
    }

}
