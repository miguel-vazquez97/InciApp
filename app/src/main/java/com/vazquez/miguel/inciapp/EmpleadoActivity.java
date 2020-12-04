package com.vazquez.miguel.inciapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDate;

public class EmpleadoActivity extends AppCompatActivity {

    protected MyApplication app;
    private Socket socket;

    protected InputStream leerServidor;
    protected OutputStream enviarServidor;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;

    int id_incidencia;
    protected Button botonUbicacion, botonEnviar, botonTomarFoto;
    protected EditText textDescripcionArreglo;
    double latitud, longitud;
    protected ImageView imagenArreglo;
    String descrip_arreglo_inci;
    //private static final String DIRECTORIO_IMAGEN = "/inciApp/imagenes/";
    private static final int COD_FOTO = 20;
    private String path;
    File fileImagen;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_empleado_incidencia);
        getSupportActionBar().setTitle(getResources().getString(R.string.EnArreglo));

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

        botonUbicacion = findViewById(R.id.emp_inci_boton_ubicacion);
        botonUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), MapsActivity.class);
                intent.putExtra("latitud_ubicacion", latitud);
                intent.putExtra("longitud_ubicacion", longitud);
                startActivity(intent);
            }
        });

        botonEnviar = findViewById(R.id.emp_inci_boton_enviar);
        textDescripcionArreglo = findViewById(R.id.descripcion_arreglo);
        botonTomarFoto = findViewById(R.id.boton_foto_arreglo);
        imagenArreglo = (ImageView) findViewById(R.id.imagen_arreglo);

        EmpleadoIncidenciasTask empleadoIncidenciasTask = new EmpleadoIncidenciasTask();
        empleadoIncidenciasTask.execute();
    }

    public void tomarFoto(View v){
        //Comprobamos que los permisos están asignados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1000);
        }else{

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1000);
            }else{


                try {

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // creamos la imagen
                    fileImagen = createImageFile();

                    uri = FileProvider.getUriForFile(EmpleadoActivity.this, BuildConfig.APPLICATION_ID + ".provider",fileImagen);

                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    //Lanzamos la cámara
                    startActivityForResult(cameraIntent,COD_FOTO);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private File createImageFile() throws IOException{

        long consecutivo = System.currentTimeMillis() / 1000;
        String pictureFile = Long.toString(consecutivo);
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES+DIRECTORIO_IMAGEN);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(pictureFile, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        path = image.getAbsolutePath();

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == COD_FOTO && resultCode == RESULT_OK){
            File imgFile = new File(path);
            //comprobamos que existe la imagen
            //la envimos al ImageView
            if(imgFile.exists()){
                imagenArreglo.setImageURI(Uri.fromFile(imgFile));
                imagenArreglo.setVisibility(View.VISIBLE);
            }
        }

    }

    protected void comprobarArregloIncidencia(View v){

        descrip_arreglo_inci = String.valueOf(textDescripcionArreglo.getText());
        if(descrip_arreglo_inci.length()<1){
            Toast.makeText(getApplication(),"Introduzca una descripción del arreglo.",Toast.LENGTH_LONG).show();
            return;
        }

        if(imagenArreglo.getDrawable() == null){
            Toast.makeText(getApplication(),"Suba una foto del arreglo.",Toast.LENGTH_LONG).show();
            return;
        }

        EnviarArregloIncidenciaTask enviarArregloIncidenciaTask = new EnviarArregloIncidenciaTask();
        enviarArregloIncidenciaTask.execute();

    }

    class EmpleadoIncidenciasTask extends AsyncTask<Void, Void, IncidenciaDetalle> {

        ProgressBar progressBar;
        String envioServidor;
        byte[] envioSer, respuestaSer;

        @Override
        protected void onPreExecute(){
            progressBar = findViewById(R.id.progressbar_emple_inci);
        }

        @Override
        protected IncidenciaDetalle doInBackground(Void... voids) {
            envioServidor = "59||" + id_incidencia + "||";
            IncidenciaDetalle incidenciaDetalle = null;

            try {
                while(leerServidor.available()>0){
                    leerServidor.read(respuestaSer = new byte[leerServidor.available()]);
                }

                envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

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
                    inputStream.read(arrayBytesBase64);
                    cadena = new String(arrayBytesBase64);
                    base64 += cadena;
                }

                byte[] arrayBytes = Base64.decode(base64, Base64.DEFAULT);
                String cadena_detalles_incidencia = new String(arrayBytes);
                JSONObject jsonObject = new JSONObject(cadena_detalles_incidencia);

                String fecha = jsonObject.getString("fecha");
                String[] amd = fecha.split("-");
                LocalDate localDate = LocalDate.of(Integer.parseInt(amd[0]), Integer.parseInt(amd[1]), Integer.parseInt(amd[2]));

                String imagenBase64;
                byte[] byteArray;
                Bitmap bmpImage;
                if(!jsonObject.getString("imagen").isEmpty()){
                    imagenBase64 = jsonObject.getString("imagen");
                    byteArray = Base64.decode(imagenBase64, Base64.DEFAULT);
                    bmpImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                }else{
                    bmpImage = null;
                }

                incidenciaDetalle = new IncidenciaDetalle(jsonObject.getString("estado"), jsonObject.getString("ubicacion"), jsonObject.getString("direccion"), jsonObject.getString("descripcion"), jsonObject.getString("tipo"), localDate, jsonObject.getString("descripcionEstadoIncidencia"), bmpImage);


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

            TextView textViewTipo = findViewById(R.id.emp_inci_tipo);
            textViewTipo.setText(incidenciaDetalle.getTipo());
            TextView textViewFecha = findViewById(R.id.emp_inci_fecha);
            textViewFecha.setText(incidenciaDetalle.getDate().toString());

            TextView textViewDescripcion = findViewById(R.id.emp_inci_descripcion);
            textViewDescripcion.setText(incidenciaDetalle.getDescripcion());

            ImageView imageView = findViewById(R.id.emp_inci_imagen);
            imageView.setImageBitmap(incidenciaDetalle.getImage());

            TextView textViewUbicacion = findViewById(R.id.emp_inci_ubicacion);
            textViewUbicacion.setText(incidenciaDetalle.getDireccion());

            //if(incidenciaDetalle.getDescripcionEstadoIncidencia()!=null){
            if(!incidenciaDetalle.getDescripcionEstadoIncidencia().equals("null")){
                TextView textViewDescripcionEstado = findViewById(R.id.text_mal_arreglo);
                textViewDescripcionEstado.setVisibility(View.VISIBLE);
                textViewDescripcionEstado.setText(incidenciaDetalle.getDescripcionEstadoIncidencia());
            }

            textDescripcionArreglo.setVisibility(View.VISIBLE);
            botonTomarFoto.setVisibility(View.VISIBLE);
            botonTomarFoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tomarFoto(v);
                }
            });

            botonUbicacion.setVisibility(View.VISIBLE);
            botonEnviar.setVisibility(View.VISIBLE);
            botonEnviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    comprobarArregloIncidencia(v);

                }
            });

        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(EmpleadoActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    class EnviarArregloIncidenciaTask extends AsyncTask<Void, Void, Boolean> {

        AlertDialog.Builder builder;
        AlertDialog progressDialog;
        ProgressBar progressBar;
        String envioServidor,respuestaServidor;
        String[] resServidor;
        byte[] envioSer, respuestaSer;
        boolean respuesta;

        @Override
        protected void onPreExecute(){
            respuesta=false;

            builder = new AlertDialog.Builder(EmpleadoActivity.this);
            builder.setTitle("Enviando...");

            progressBar = new ProgressBar(EmpleadoActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            progressBar.setLayoutParams(lp);
            builder.setView(progressBar);
            progressDialog = builder.create();
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            File file = new File(path);
            envioServidor = "64||comenzar_envio_datos_arreglo||"+id_incidencia+"||"+descrip_arreglo_inci+"||"+file.getName()+"||" ;

            try {
                if(leerServidor.available()>0){
                    leerServidor.read(respuestaSer = new byte[leerServidor.available()]);
                }

                envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

                respuestaServidor = inputStream.readUTF();
                resServidor = respuestaServidor.split("\\|\\|");
                if(resServidor[1].equals("esperandoImagenArregloIncidencia")){

                    //Enviamos la imagen mediante otro hilo
                    EnviarImagen enviarImagen = new EnviarImagen();
                    enviarImagen.start();
                    enviarImagen.join();

                    byte[] reciboSer = new byte[1024];
                    leerServidor.read(reciboSer);
                    respuestaServidor = new String(reciboSer);
                    resServidor = respuestaServidor.split("\\|\\|");

                    if(resServidor[1].equals("arregloIncidenciaOk")){
                        respuesta=true;
                    }
                }

            } catch (IOException ex) {
                cancel(true);
                ex.printStackTrace();
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }

            return respuesta;
        }

        @Override
        protected void onPostExecute(Boolean respuesta) {

            progressDialog.dismiss();

            if(respuesta){
                Intent intent = new Intent(getApplication(), ListadoIncidencias.class);
                intent.putExtra("tipo_incidencia", "incidencias_enArreglo");
                intent.putExtra("tipo_usuario", 3);
                Toast.makeText(getApplication(),getResources().getString(R.string.arreglo_incidencia_ok),Toast.LENGTH_LONG).show();
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(getApplication(),getResources().getString(R.string.mensaje_error),Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(EmpleadoActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    class EnviarImagen extends Thread{
        @Override
        public void run(){
            InputStream inputStream1 = null;
            File file = new File(path);
            try {
                inputStream1 = new FileInputStream(path);
                byte[] bytes;
                byte[] buffer = new byte[8192];
                int bytesRead;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while((bytesRead = inputStream1.read(buffer)) != -1){
                    output.write(buffer, 0, bytesRead);
                }
                bytes = output.toByteArray();
                outputStream.writeLong(file.length());
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
