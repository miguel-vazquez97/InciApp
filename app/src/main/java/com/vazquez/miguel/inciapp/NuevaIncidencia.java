package com.vazquez.miguel.inciapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Locale;

public class NuevaIncidencia extends AppCompatActivity {

    //private static final String DIRECTORIO_IMAGEN = "/inciApp/imagenes/";
    private static final int COD_FOTO = 20;
    private String path;

    protected Socket socket;
    protected InputStream leerServidor;
    protected OutputStream enviarServidor;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;

    protected MyApplication app;

    LocationManager locationManager;
    double longitudeGPS, latitudeGPS;
    String direccion;
    TextView ubicacionText;
    Button boton_ubicacion,boton_foto,boton_registrar_incidencia;
    ImageView imagenView;
    EditText textoDescripcion;
    Spinner spinnerTipos;


    ProgressBar progressBar;
    AlertDialog alertDialog;
    String tipo_inci;
    String descrip_inci;

    private FusedLocationProviderClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_nueva_incidencia);

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

        client = LocationServices.getFusedLocationProviderClient(this);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyle);
        progressBar.setVisibility(View.VISIBLE);
        alertDialog = new AlertDialog.Builder(NuevaIncidencia.this).create();
        alertDialog.setMessage("Obteniendo ubicación...");
        alertDialog.setCancelable(false);
        alertDialog.setView(progressBar);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ubicacionText = (TextView) findViewById(R.id.ubicacionText);
        imagenView = (ImageView) findViewById(R.id.imagen_view);
        textoDescripcion = (EditText) findViewById(R.id.descripcion_text);
        spinnerTipos = (Spinner) findViewById(R.id.spinner_tipo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_value, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipos.setAdapter(adapter);



        boton_ubicacion =(Button) findViewById(R.id.boton_ubicacion);
        boton_ubicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleGPSUpdates(v);

            }
        });

        boton_foto = (Button) findViewById(R.id.boton_foto);
        boton_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tomarFoto(v);

            }
        });

        boton_registrar_incidencia = (Button) findViewById(R.id.boton_registrar_incidencia);
        boton_registrar_incidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registrarIncidencia(v);

            }
        });

    }

    public void tomarFoto(View v){
        //pedimos permisos para poder acceder a la camara del dispositivo
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1000);
        }else{
            //permisos para poder escribir en la memorio del dispositivo
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1000);
            }else{

                try {

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File fileImagen = createImageFile();

                    Uri uri = FileProvider.getUriForFile(NuevaIncidencia.this, BuildConfig.APPLICATION_ID + ".provider", fileImagen);

                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

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

        if (image != null) {
            path = image.getAbsolutePath();
        }

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == COD_FOTO && resultCode == RESULT_OK){
            File imgFile = new File(path);
            if(imgFile.exists()){
                imagenView.setImageURI(Uri.fromFile(imgFile));
                imagenView.setVisibility(View.VISIBLE);
            }
        }


    }

    private boolean checkLocation(){
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Ubicación desactivada")
                .setMessage("Su ubicación esta desactivada.")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleGPSUpdates(View view) {
        if (!checkLocation())
            return;

        //pedimos permisos referentes a la localización del dispositivo
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        }else{

            alertDialog.show();

            client.getLastLocation().addOnSuccessListener(this,new OnSuccessListener<Location>(){

                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        longitudeGPS = location.getLongitude();
                        latitudeGPS = location.getLatitude();

                        if (longitudeGPS != 0.0 && latitudeGPS != 0.0) {
                            try {
                                Geocoder geocoder = new Geocoder(NuevaIncidencia.this, Locale.getDefault());
                                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (!list.isEmpty()) {
                                    Address address = list.get(0);
                                    direccion = address.getAddressLine(0);
                                    ubicacionText.setText(direccion);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            alertDialog.dismiss();
        }
    }

    protected void registrarIncidencia(View v){

        if(imagenView.getDrawable() == null){
            Toast.makeText(getApplication(),"Suba una foto de la incidencia.",Toast.LENGTH_LONG).show();
            return;
        }

        //bitmap = getBitmapFromDrawable(imagenView.getDrawable());

        tipo_inci = spinnerTipos.getSelectedItem().toString();

        descrip_inci = textoDescripcion.getText().toString();
        if(descrip_inci.length()<1){
            Toast.makeText(getApplication(),"Introduzca una descripción.",Toast.LENGTH_LONG).show();
            return;
        }

        if (longitudeGPS == 0.0 && latitudeGPS == 0.0) {
            Toast.makeText(getApplication(),"Obtenga su ubicación.",Toast.LENGTH_LONG).show();
            return;
        }

        RegistrarIncidenciaTask registrarIncidenciaTask = new RegistrarIncidenciaTask();
        registrarIncidenciaTask.execute();

    }

    class RegistrarIncidenciaTask extends AsyncTask<Void, Void, Boolean> {

        ProgressBar progressBar;
        Button boton_reg;

        String envioServidor;
        String respuestaServidor;
        String[] resServidor;
        byte[] envioSer, respuestaSer;

        @Override
        protected void onPreExecute(){
            progressBar = findViewById(R.id.progressbar_registrar_incidencia);
            progressBar.setVisibility(View.VISIBLE);
            boton_reg = findViewById(R.id.boton_registrar_incidencia);
            boton_reg.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            File file = new File(path);
            envioServidor = "53||comenzar_registro||"+file.length()+"||"+file.getName()+"||";

            try{
                while(leerServidor.available()>0){
                    leerServidor.read(respuestaSer = new byte[leerServidor.available()]);
                }

                //nos comunicamos con el servidor
                envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

                //entramos en el while
                while(true){
                    int time=0;
                    //esperamos a recibir la comunicación con el server
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
                    //leeremos lo que nos haya enviado el server
                    respuestaServidor = inputStream.readUTF();
                    //dependiendo de lo que nos haya perdido el server
                    //le enviaremos la imagen o los datos
                    switch(respuestaServidor){
                        case "70||esperandoImagenNuevaRegistrada||":
                            //Enviamos la imagen mediante otro hilo
                            EnviarImagen enviarImagen = new EnviarImagen(file);
                            enviarImagen.start();
                            enviarImagen.join();
                            break;

                        case "71||enviarDatosNuevaRegistrada||":
                            String datosIncidencia = latitudeGPS+";"+longitudeGPS+"||"+direccion+"||"+descrip_inci+"||"+tipo_inci+"||"+app.getCorreo()+"||";
                            outputStream.writeUTF(datosIncidencia);
                            break;

                        //si todo ha ido correctamente a la hora de enviar los datos o ha habido algún error
                        //saldremos del switch y del while para informar al usuario
                        default:
                            return true;
                    }

                }



            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return true;

        }

        @Override
        protected void onPostExecute(Boolean value){
            progressBar.setVisibility(View.GONE);
            boton_reg.setVisibility(View.VISIBLE);

            if(value==null){
                Toast.makeText(getApplication(), getApplication().getString(R.string.mensaje_error_conection_server), Toast.LENGTH_LONG).show();
                return;
            }

            //mostramos el mensaje al usuario dependiendo de la respuesta recibida por parte del server
            resServidor = respuestaServidor.split("\\|\\|");
            switch (resServidor[1]){
                case "incidenciaOk":
                    Toast.makeText(getApplication(),getApplication().getString(R.string.incidenciaOk),Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case "incidenciaYaRegistrada":
                    Toast.makeText(getApplication(),getApplication().getString(R.string.incidenciaYaRegistrada),Toast.LENGTH_LONG).show();
                    break;
                case "incidenciaDenegada":
                    Toast.makeText(getApplication(),getApplication().getString(R.string.incidenciaDenegada),Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(getApplication(),getApplication().getString(R.string.errorImagen),Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(NuevaIncidencia.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    class EnviarImagen extends Thread{

        File file;

        EnviarImagen(File file){
            this.file = file;
        }

        @Override
        public void run(){
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(path);
                byte[] bytes;
                byte[] buffer = new byte[8192];
                int bytesRead;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while((bytesRead = inputStream.read(buffer)) != -1){
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