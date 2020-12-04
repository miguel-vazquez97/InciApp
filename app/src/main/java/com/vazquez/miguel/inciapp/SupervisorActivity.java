package com.vazquez.miguel.inciapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SupervisorActivity extends AppCompatActivity {

    MyApplication app;
    int tipoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_supervisor);

        app = (MyApplication) getApplication();

        Bundle datos = this.getIntent().getExtras();
        tipoUsuario = datos.getInt("tipo_usuario");

        Button boton_enTramite = findViewById(R.id.boton_inci_enTramite);
        boton_enTramite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ListadoIncidencias.class);
                intent.putExtra("tipo_incidencia", "incidencias_enTramite");
                intent.putExtra("tipo_usuario", tipoUsuario);
                startActivity(intent);
            }
        });

        Button boton_validarArreglo = findViewById(R.id.boton_inci_validarArreglo);
        boton_validarArreglo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ListadoIncidencias.class);
                intent.putExtra("tipo_incidencia", "incidencias_validarArreglo");
                intent.putExtra("tipo_usuario", tipoUsuario);
                startActivity(intent);
            }
        });
    }
}
