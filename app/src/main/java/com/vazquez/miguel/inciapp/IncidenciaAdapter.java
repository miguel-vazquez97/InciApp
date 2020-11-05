package com.vazquez.miguel.inciapp;

import android.app.Activity;
import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.ViewHolderIncidencias>{
    private List<IncidenciaRow> incidencias;
    private int item_row;
    private Activity activity;
    private OnItemClickListener listener;
    private int tipoUsuario;

    MyApplication app;

    public IncidenciaAdapter(List<IncidenciaRow> incidencias, int item_row, ListadoIncidencias activity, int tipoUsuario, Application app, OnItemClickListener listener) {
        this.incidencias = incidencias;
        this.item_row = item_row;
        this.activity = activity;
        this.listener = listener;
        this.tipoUsuario = tipoUsuario;

        this.app = (MyApplication) app;
    }

    @NonNull
    @Override
    public IncidenciaAdapter.ViewHolderIncidencias onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(item_row,viewGroup,false);
        return new ViewHolderIncidencias(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenciaAdapter.ViewHolderIncidencias viewHolder, final int i) {
        final IncidenciaRow incidencia = incidencias.get(i);

        viewHolder.tipo_incidencia.setText(incidencia.getTipo());
        viewHolder.estado_incidencia.setText(incidencia.getEstado());
        viewHolder.ubicacion_incidencia.setText(incidencia.getDireccion());

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(incidencia, i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    public class ViewHolderIncidencias extends RecyclerView.ViewHolder{

        TextView tipo_incidencia;
        TextView estado_incidencia;
        TextView ubicacion_incidencia;
        CardView cardView;

        public ViewHolderIncidencias(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            if(tipoUsuario==4){
                cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
            }else{
                cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
            }

            tipo_incidencia= itemView.findViewById(R.id.tipo_inci);
            estado_incidencia= itemView.findViewById(R.id.estado_inci);
            ubicacion_incidencia= itemView.findViewById(R.id.ubicacion_inci);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(IncidenciaRow incidencia, int position);
    }
}
