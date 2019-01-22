package com.example.a21752434.primerfirebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.a21752434.primerfirebase.javabeans.Mensaje;
import com.example.a21752434.primerfirebase.javabeans.MensajeAdaptador;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etMensaje;
    private Button btnEnviar;
    private RecyclerView rvMensajes;
    private MensajeAdaptador adapter;
    private LinearLayoutManager llm;

    private ArrayList<Mensaje> datos;

    private DatabaseReference dbR;
    private ChildEventListener cel;

    private String remitente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remitente = "ANONIMO";

        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        rvMensajes = findViewById(R.id.rvMensajes);

        datos = new ArrayList<Mensaje>();

        adapter = new MensajeAdaptador(datos);
        llm = new LinearLayoutManager(this);
        rvMensajes.setLayoutManager(llm);
        rvMensajes.setAdapter(adapter);
        rvMensajes.setItemAnimator(new DefaultItemAnimator());

        dbR = FirebaseDatabase.getInstance().getReference().child("mensaje");

        addChildEventListener();

    }

    private void addChildEventListener() {
        if(cel == null) {
            cel = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    System.out.println("Nuevo mensaje");
                    Mensaje m = dataSnapshot.getValue(Mensaje.class);
                    datos.add(m);
                    adapter.notifyItemChanged(datos.size()-1);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    System.out.println("Mensaje modificado");
                    Mensaje m = dataSnapshot.getValue(Mensaje.class);

                    int pos = 0;
                    for(int i = 0; i < datos.size(); i++) {
                        if(datos.get(i).getTextoMsj().equals(m.getTextoMsj())) {        // se busca el mismo mensaje
                            datos.set(i, m);
                            pos = i;
                        }
                    }
                    adapter.notifyItemChanged(pos);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    System.out.println("Mensaje borrado: "
                            +dataSnapshot.getValue(Mensaje.class).getTextoMsj());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            dbR.addChildEventListener(cel);
        }
    }

    public void enviar(View v) {
        Mensaje msj = new Mensaje(etMensaje.getText().toString(), remitente);
        String clave = dbR.push().getKey();
        dbR.child(clave).setValue(msj);
        //dbR.push().setValue(msj);

        etMensaje.setText("");
    }

    public void aniadir(View v) {
        Mensaje msj = new Mensaje("Bienvenido a Firebase", remitente);
        dbR.child("Bienvenida").setValue(msj);
    }

    public void modificar(View v) {
        Map<String, Object> mapa = new HashMap<String, Object>();
        mapa.put("Bienvenida/remitente", null);                                                     //accede a la propiedad remitente de la clave Bienvenida
        mapa.put("Despedida", new Mensaje("Hasta pronto!", null));
        dbR.updateChildren(mapa);
    }

    public void borrar(View v) {
        dbR.removeValue();
        adapter.clear();
    }

    /**
     * Liberar recursos cuando se
     * pausa la actividad
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(cel != null) {
            dbR.removeEventListener(cel);
            cel = null;
        }
        adapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addChildEventListener();
    }
}
