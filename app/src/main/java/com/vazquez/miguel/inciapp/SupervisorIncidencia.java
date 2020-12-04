package com.vazquez.miguel.inciapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SupervisorIncidencia extends AppCompatActivity {

    protected MyApplication app;
    private Socket socket;

    protected InputStream leerServidor;
    protected OutputStream enviarServidor;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;

    protected Button botonUbicacion, botonValidar, botonDenegar;
    int id_incidencia, tipoUsuario;
    double latitud, longitud;
    String opcion,tipoEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_supervisor_incidencia);

        Bundle datos = this.getIntent().getExtras();
        id_incidencia = datos.getInt("id_incidencia");
        tipoUsuario = datos.getInt("tipo_usuario");
        //con la variable tipoEstado sabremos el tipo de incidencia con el que tratamos EnTramite/ValidarArreglo
        tipoEstado = datos.getString("tipo_estado");

        if(tipoEstado.equals("enTramite")){
            getSupportActionBar().setTitle(getResources().getString(R.string.EnTramite));
        }else{
            getSupportActionBar().setTitle(getResources().getString(R.string.ValidarArreglo));
        }

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

        botonUbicacion = findViewById(R.id.sup_inci_boton_ubicacion);
        botonUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), MapsActivity.class);
                intent.putExtra("latitud_ubicacion", latitud);
                intent.putExtra("longitud_ubicacion", longitud);
                startActivity(intent);
            }
        });
        botonValidar = findViewById(R.id.sup_inci_boton_validar);
        botonDenegar = findViewById(R.id.sup_inci_boton_denegar);

        SupervisorIncidenciasTask supervisorIncidenciasTask = new SupervisorIncidenciasTask();
        supervisorIncidenciasTask.execute();

    }

    class SupervisorIncidenciasTask extends AsyncTask<Void, Void, IncidenciaDetalle> {

        ProgressBar progressBar;
        AlertDialog dialog;
        String envioServidor;
        byte[] envioSer, respuestaSer;

        @Override
        protected void onPreExecute(){
            progressBar = findViewById(R.id.progressbar_sup_inci);
        }

        @Override
        protected IncidenciaDetalle doInBackground(Void... voids) {
            envioServidor = "61||" + id_incidencia + "||" + tipoEstado + "||";
            IncidenciaDetalle incidenciaDetalle = null;

            try {
                while(leerServidor.available()>0){
                    leerServidor.read(respuestaSer = new byte[leerServidor.available()]);
                }

                envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

                //tamano que tendra nuestra cadena en base64
                int size = inputStream.readInt();

                int time=0;
                while(leerServidor.available()<1 && time<4000){
                    try {
                        Thread.sleep(500);
                        time += 500;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(time==4000){
                    return null;
                }

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
                //creamos lo que será nuestro JSONObject en cadena
                String cadena_detalles_incidencia = new String(arrayBytes);
                //creamos el JSONObject a partir de la cadena anterior
                JSONObject jsonObject = new JSONObject(cadena_detalles_incidencia);

                String fecha = jsonObject.getString("fecha");
                String[] amd = fecha.split("-");
                LocalDate localDate = LocalDate.of(Integer.parseInt(amd[0]), Integer.parseInt(amd[1]), Integer.parseInt(amd[2]));

                String imagenBase64;
                byte[] byteArray;
                Bitmap bmpImage, bmpImageArreglo;
                if(!jsonObject.getString("imagen").isEmpty()){
                    imagenBase64 = jsonObject.getString("imagen");
                    byteArray = Base64.decode(imagenBase64, Base64.DEFAULT);
                    bmpImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                }else{
                    bmpImage = null;
                }

                if(tipoEstado.equals("enTramite")) {
                    incidenciaDetalle = new IncidenciaDetalle(jsonObject.getString("estado"), jsonObject.getString("ubicacion"), jsonObject.getString("direccion"), jsonObject.getString("descripcion"), jsonObject.getString("tipo"), localDate, bmpImage);
                }else{
                    if(!jsonObject.getString("imagenArreglo").isEmpty()){
                        imagenBase64 = jsonObject.getString("imagenArreglo");
                        byteArray = Base64.decode(imagenBase64, Base64.DEFAULT);
                        bmpImageArreglo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    }else{
                        bmpImageArreglo = null;
                    }
                    incidenciaDetalle = new IncidenciaDetalle(jsonObject.getString("estado"), jsonObject.getString("ubicacion"), jsonObject.getString("direccion"), jsonObject.getString("descripcion"), jsonObject.getString("tipo"), localDate, bmpImage, jsonObject.getString("descripcionArreglo"), jsonObject.getString("fechaArreglo"), bmpImageArreglo);
                }

            } catch (IOException ex) {
                cancel(true);
                ex.printStackTrace();
            }catch(JSONException jsonex){
                jsonex.printStackTrace();
            }

            return incidenciaDetalle;
        }

        @Override
        protected void onPostExecute(IncidenciaDetalle incidenciaDetalle) {
            progressBar.setVisibility(View.GONE);

            if(incidenciaDetalle==null){
                Toast.makeText(getApplication(), getApplication().getString(R.string.mensaje_error_conection_server), Toast.LENGTH_LONG).show();
                return;
            }

            String[] lon_lat = incidenciaDetalle.getUbicacion().split(";");
            latitud = Double.parseDouble(lon_lat[0]);
            longitud = Double.parseDouble(lon_lat[1]);

            TextView textViewTipo = findViewById(R.id.sup_inci_tipo);
            textViewTipo.setText(incidenciaDetalle.getTipo());
            TextView textViewFecha = findViewById(R.id.sup_inci_fecha);
            textViewFecha.setText(incidenciaDetalle.getDate().toString());

            TextView textViewDescripcion = findViewById(R.id.sup_inci_descripcion);
            textViewDescripcion.setText(incidenciaDetalle.getDescripcion());

            ImageView imageView = findViewById(R.id.sup_inci_imagen);
            imageView.setImageBitmap(incidenciaDetalle.getImage());
            TextView textViewUbicacion = findViewById(R.id.sup_inci_ubicacion);
            textViewUbicacion.setText(incidenciaDetalle.getDireccion());

            if(!tipoEstado.equals("enTramite")){
                TextView textViewDetallesArreglo = findViewById(R.id.sup_inci_descripcion_arreglo);
                textViewDetallesArreglo.setText(incidenciaDetalle.getDescripcionArreglo());
                TextView textViewFechaArreglo = findViewById(R.id.sup_inci_fecha_arreglo);
                textViewFechaArreglo.setText(incidenciaDetalle.getFechaArreglo());
                ImageView imageViewArreglo = findViewById(R.id.sup_inci_imagen_arreglo);
                imageViewArreglo.setImageBitmap(incidenciaDetalle.getImageArreglo());

                View separador = findViewById(R.id.separadorArreglo);
                separador.setVisibility(View.VISIBLE);
                textViewFechaArreglo.setVisibility(View.VISIBLE);
                textViewDetallesArreglo.setVisibility(View.VISIBLE);
                imageViewArreglo.setVisibility(View.VISIBLE);
            }

            botonUbicacion.setVisibility(View.VISIBLE);
            botonValidar.setVisibility(View.VISIBLE);
            botonValidar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SupervisorIncidencia.this);
                    opcion = "validar";
                    if(tipoEstado.equals("enTramite")) {
                        builder.setMessage(getResources().getString(R.string.validar_incidencia));
                    }else {
                        builder.setMessage(getResources().getString(R.string.validar_arreglo_incidencia));
                    }

                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            OpcionIncidenciaTask opcionIncidenciaTask = new OpcionIncidenciaTask(opcion);
                            opcionIncidenciaTask.execute();
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialog = builder.create();
                    dialog.show();
                }
            });

            botonDenegar.setVisibility(View.VISIBLE);
            botonDenegar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SupervisorIncidencia.this);
                    if(tipoEstado.equals("enTramite")){
                        builder.setMessage(getResources().getString(R.string.denegar_incidencia));
                    }else {
                        builder.setMessage(getResources().getString(R.string.denegar_arreglo_incidencia));
                    }

                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createPopUp();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialog = builder.create();
                    dialog.show();
                }
            });
        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SupervisorIncidencia.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void createPopUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.popup_supervisor_incidencia,null);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        final TextView cabecera_popup = view.findViewById(R.id.popupdenegar_cabecera);
        cabecera_popup.setText(getResources().getString(R.string.titulo_denegarpopup));

        final EditText descripcion_popup = view.findViewById(R.id.popupdenegar_descripcion);

        Button boton_aceptar = view.findViewById(R.id.popupdenegar_boton);

        boton_aceptar.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(descripcion_popup.getText())){

                    opcion="denegar";
                    String descripcion = String.valueOf(descripcion_popup.getText());
                    OpcionIncidenciaTask opcionIncidenciaTask = new OpcionIncidenciaTask(opcion,descripcion);
                    opcionIncidenciaTask.execute();

                }else{
                    Toast.makeText(getApplication(),getApplication().getString(R.string.error_denegarpopup),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    class OpcionIncidenciaTask extends AsyncTask<Void, Void, String[]> {

        AlertDialog.Builder builder;
        AlertDialog progressDialog;
        ProgressBar progressBar;
        String envioServidor,respuestaServidor,opcion,descripcion;
        String[] resServidor;
        byte[] envioSer, respuestaSer;

        public OpcionIncidenciaTask(String opcion){
            this.opcion = opcion;
        }

        public OpcionIncidenciaTask(String opcion, String descripcion){
            this.opcion = opcion;
            this.descripcion = descripcion;
        }

        @Override
        protected void onPreExecute(){
            builder = new AlertDialog.Builder(SupervisorIncidencia.this);

            builder.setTitle("Enviando...");

            progressBar = new ProgressBar(SupervisorIncidencia.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            progressBar.setLayoutParams(lp);
            builder.setView(progressBar);
            progressDialog = builder.create();
            progressDialog.show();
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            if(tipoEstado.equals("enTramite")){
                envioServidor = "62||" + id_incidencia + "||";
            }else{
                envioServidor = "63||" + id_incidencia + "||";
            }

            if(opcion.equals("validar")){
                envioServidor += "validar_incidencia||null||";
            }else{
                envioServidor += "denegar_incidencia||"+descripcion+"||";
            }

            try {
                while(leerServidor.available()>0){
                    leerServidor.read(respuestaSer = new byte[leerServidor.available()]);
                }

                envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

                int time=0;
                while(leerServidor.available()<1 && time<4000){
                    try {
                        Thread.sleep(500);
                        time += 500;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(time==4000){
                    return null;
                }

                respuestaSer = new byte[inputStream.available()];
                leerServidor.read(respuestaSer);
                respuestaServidor = new String(respuestaSer);
                resServidor = respuestaServidor.split("\\|\\|");

            } catch (IOException ex) {
                cancel(true);
                ex.printStackTrace();
            }

            return resServidor;
        }

        @Override
        protected void onPostExecute(String[] respuesta) {
            progressDialog.dismiss();

            if(respuesta==null){
                Toast.makeText(getApplication(), getApplication().getString(R.string.mensaje_error_conection_server), Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(getApplication(), ListadoIncidencias.class);
            if(tipoEstado.equals("enTramite")) {
                intent.putExtra("tipo_incidencia", "incidencias_enTramite");
            }else{
                intent.putExtra("tipo_incidencia", "incidencias_validarArreglo");
            }
            intent.putExtra("tipo_usuario", tipoUsuario);

            switch(respuesta[1]){
                case "opcionEnTramiteIncidenciaValidada":
                    Toast.makeText(getApplication(),getResources().getString(R.string.validar_incidencia_ok),Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                    break;

                case "opcionEnTramiteIncidenciaDenegada":
                    Toast.makeText(getApplication(),getResources().getString(R.string.denegar_incidencia_ok),Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                    break;

                case "opcionValidarArregloIncidenciaValidada":
                    Toast.makeText(getApplication(),getResources().getString(R.string.validar_arreglo_incidencia_ok),Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                    break;

                case "opcionValidarArregloIncidenciaDenegada":
                    Toast.makeText(getApplication(),getResources().getString(R.string.denegar_arreglo_incidencia_ok),Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                    break;

                default:
                    Toast.makeText(getApplication(),getResources().getString(R.string.mensaje_error),Toast.LENGTH_LONG).show();
                    break;
            }

        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SupervisorIncidencia.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

}
