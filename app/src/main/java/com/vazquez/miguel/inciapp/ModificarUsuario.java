package com.vazquez.miguel.inciapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ModificarUsuario extends AppCompatActivity {

    protected MyApplication app;
    protected int tipoUsuario;
    private Socket socket;

    protected InputStream leerServidor;
    protected OutputStream enviarServidor;
    protected DataInputStream inputStream;

    protected Usuario usuarioSeleccionado, usuarioModificado;
    EditText text_contrasena, text_nombre, text_apellido, text_dni, text_tlf;
    TextView text_correo, text_departamento;
    LinearLayout linear_departamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_modificar_usuario);

        app = (MyApplication) getApplication();

        Bundle datos = this.getIntent().getExtras();
        tipoUsuario = datos.getInt("tipo_usuario");

        Button boton_modificar = findViewById(R.id.boton_modificar);
        boton_modificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compararUsuario();
            }
        });

        text_correo = findViewById(R.id.correo_text_mod);
        text_contrasena = findViewById(R.id.contra_text_mod);
        text_nombre = findViewById(R.id.nombre_text_mod);
        text_apellido = findViewById(R.id.apellidos_text_mod);
        text_dni = findViewById(R.id.dni_text_mod);
        text_tlf = findViewById(R.id.tlf_text_mod);
        text_departamento = findViewById(R.id.departamento_text_mod);
        linear_departamento = findViewById(R.id.linear_departamento);

        DetallesUsuarioTask detallesUsuarioTask = new DetallesUsuarioTask();
        detallesUsuarioTask.execute();

    }

    public void insertarDetalles(){
        text_correo.setText(usuarioSeleccionado.getCorreo());
        text_contrasena.setText(usuarioSeleccionado.getContrasena());
        text_nombre.setText(usuarioSeleccionado.getNombre());
        text_apellido.setText(usuarioSeleccionado.getApellido());
        text_dni.setText(usuarioSeleccionado.getDni());
        text_tlf.setText(String.valueOf(usuarioSeleccionado.getTlf()));
        if(tipoUsuario==2 || tipoUsuario==3){
            text_departamento.setText(usuarioSeleccionado.getDepartamento());
            linear_departamento.setVisibility(View.VISIBLE);
        }
    }

    public void compararUsuario(){

        boolean modificar_usuario = true;
        String correo, contrasena, nombre, apellido, dni, tlf, departamento = "NULL";
        correo = String.valueOf(text_correo.getText());
        if(tipoUsuario==2 || tipoUsuario==3){
            departamento = String.valueOf(text_departamento.getText());
        }

        contrasena = String.valueOf(text_contrasena.getText());
        if(contrasena.isEmpty()){
            text_contrasena.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_contrasena.setTextColor(Color.WHITE);
            modificar_usuario = false;
        }else{
            text_contrasena.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_contrasena.setTextColor(Color.rgb(123, 164, 168));
        }

        nombre = String.valueOf(text_nombre.getText());
        if(nombre.isEmpty()){
            text_nombre.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_nombre.setTextColor(Color.WHITE);
            modificar_usuario = false;
        }else{
            text_nombre.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_nombre.setTextColor(Color.rgb(123, 164, 168));
        }

        apellido = String.valueOf(text_apellido.getText());
        if(apellido.isEmpty()){
            text_apellido.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_apellido.setTextColor(Color.WHITE);
            modificar_usuario = false;
        }else{
            text_apellido.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_apellido.setTextColor(Color.rgb(123, 164, 168));
        }

        dni = String.valueOf(text_dni.getText());
        if(dni.isEmpty()){
            text_dni.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_dni.setTextColor(Color.WHITE);
            modificar_usuario = false;
        }else{
            text_dni.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_dni.setTextColor(Color.rgb(123, 164, 168));
        }

        tlf = String.valueOf(text_tlf.getText());
        if(tlf.isEmpty() || tlf.length()<9){
            text_tlf.setBackgroundColor(Color.rgb( 228, 83, 94));
            text_tlf.setTextColor(Color.WHITE);
            modificar_usuario = false;
        }else{
            text_tlf.setBackgroundColor(Color.rgb( 221, 222, 222));
            text_tlf.setTextColor(Color.rgb(123, 164, 168));
        }

        if(modificar_usuario) {
            usuarioModificado = new Usuario(correo, contrasena, nombre, apellido, dni, Integer.parseInt(tlf), departamento);

            if(usuarioModificado.compareTo(usuarioSeleccionado) > 0){
                ModificarUsuarioTask modificarUsuarioTask = new ModificarUsuarioTask();
                modificarUsuarioTask.execute();
            }
        }

    }

    class DetallesUsuarioTask extends AsyncTask<String, Void, Usuario> {
        ProgressBar progressBar;
        String envioServidor, respuestaServidor;
        byte[] envioSer, respuestaSer;

        @Override
        protected void onPreExecute(){
            progressBar = findViewById(R.id.progressbar_modificar);

            socket = app.getSocket();

            try {
                leerServidor = socket.getInputStream();
                inputStream = new DataInputStream(socket.getInputStream());
                enviarServidor = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Usuario doInBackground(String... strings) {
            envioServidor = "66||" + app.getCorreo() + "||";

            Usuario user = null;

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

                respuestaServidor = inputStream.readUTF();
                JSONObject object = new JSONObject(respuestaServidor);

                String departamento = "NULL";
                if(tipoUsuario==2 || tipoUsuario==3){
                    departamento = object.get("departamento").toString();
                }

                user = new Usuario(object.getString("correo"), object.getString("contrasena"),
                        object.getString("nombre"), object.getString("apellido"), object.getString("dni"),
                        Integer.parseInt(object.getString("tlf")), departamento);

            }catch (IOException ex) {
                cancel(true);
                ex.printStackTrace();
            }catch(JSONException jsonex){
                jsonex.printStackTrace();
            }

            return user;
        }

        @Override
        protected void onPostExecute(Usuario user){
            progressBar.setVisibility(View.GONE);

            if(user==null){
                Toast.makeText(getApplication(), getApplication().getString(R.string.mensaje_error_conection_server), Toast.LENGTH_LONG).show();
                return;
            }

            LinearLayout linearVentanaModificar = findViewById(R.id.linear_ventana_modificar);
            linearVentanaModificar.setVisibility(View.VISIBLE);

            usuarioSeleccionado = user;
            insertarDetalles();

        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ModificarUsuario.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }


    class ModificarUsuarioTask extends AsyncTask<Void, Void, String> {
        String envioServidor, respuestaServidor;
        byte[] envioSer, respuestaSer;
        String[] resServidor;
        ProgressBar progressBarBoton;
        Button boton_modificar;

        @Override
        protected void onPreExecute() {
            boton_modificar = findViewById(R.id.boton_modificar);
            boton_modificar.setVisibility(View.GONE);
            progressBarBoton = findViewById(R.id.progressbar_boton_modificar);
            progressBarBoton.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            envioServidor = "67||"+ usuarioModificado.getCorreo()+"||"+ usuarioModificado.getContrasena()+"||"+ usuarioModificado.getNombre()+"||"+ usuarioModificado.getApellido()+"||"+ usuarioModificado.getDni()+"||"+ usuarioModificado.getTlf()+"||";

            try{
                while(leerServidor.available()>0){
                    leerServidor.read(respuestaSer = new byte[leerServidor.available()]);
                }

                envioSer = envioServidor.getBytes();
                enviarServidor.write(envioSer);
                enviarServidor.flush();

                int time = 0;
                while (leerServidor.available() < 1 && time < 4000) {
                    try {
                        Thread.sleep(500);
                        time += 500;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (time == 4000) {
                    return null;
                }

                respuestaSer = new byte[leerServidor.available()];
                leerServidor.read(respuestaSer);
                respuestaServidor = new String(respuestaSer);


            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
            }

            return respuestaServidor;

        }

        @Override
        protected void onPostExecute(String respuesta) {
            progressBarBoton.setVisibility(View.GONE);
            boton_modificar.setVisibility(View.VISIBLE);
            if(respuesta==null){
                Toast.makeText(getApplication(), getApplication().getString(R.string.mensaje_error_conection_server), Toast.LENGTH_LONG).show();
                return;
            }

            resServidor = respuesta.split("\\|\\|");
            if(resServidor[0].equals("73") && resServidor[1].equals("modificarUsuarioOk")){
                usuarioSeleccionado = usuarioModificado;
                insertarDetalles();
                Toast.makeText(getApplication(), getApplication().getString(R.string.usuario_modificado), Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplication(), getApplication().getString(R.string.mensaje_error_conection_server), Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onCancelled() {
            app.setCorreo(null);
            Toast.makeText(getApplication(), getApplication().getString(R.string.logout_mensaje), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ModificarUsuario.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }
}

