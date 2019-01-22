package com.example.a21752434.primerfirebase.javabeans;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.a21752434.primerfirebase.R;

import java.util.ArrayList;

public class MensajeAdaptador extends RecyclerView.Adapter<MensajeAdaptador.MensajeViewHolder> {

    private ArrayList<Mensaje> lista;

    public MensajeAdaptador(ArrayList<Mensaje> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_mensaje, viewGroup, false);
        MensajeViewHolder mvh = new MensajeViewHolder(v);

        return mvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder mensajeViewHolder, int i) {
        mensajeViewHolder.bindMensaje(lista.get(i));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MensajeViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTexto;
        private TextView tvRmte;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTexto = itemView.findViewById(R.id.tvTextoMj);
            tvRmte = itemView.findViewById(R.id.tvRemitente);
        }

        public void bindMensaje(Mensaje m) {
            tvTexto.setText(m.getTextoMsj());
            tvRmte.setText(m.getRemitente());
        }
    } //fin clase interna

    public void clear() {
        lista.clear();
    }

}
