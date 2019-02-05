package com.example.a21752434.primerfirebase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a21752434.primerfirebase.javabeans.Mensaje;
import com.example.a21752434.primerfirebase.javabeans.MensajeAdaptador;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etMensaje;
    private Button btnEnviar;
    private RecyclerView rvMensajes;
    private MensajeAdaptador adapter;
    private LinearLayoutManager llm;

    private ArrayList<Mensaje> datos;

    /*DATABASE*/
    private DatabaseReference dbR;
    private ChildEventListener cel;

    private String remitente;

    /*Autentificación*/
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public static final int RC_SIGN_IN = 1;

    /*STORAGE*/
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFotoStorageRef;

    public static final int RC_PHOTO_ADJ = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //remitente = "ANONIMO";

        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        rvMensajes = findViewById(R.id.rvMensajes);

        /*AUTENTICACIÓN*/
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) { // el usuario está logado
                    Toast.makeText(MainActivity.this, "Ya estás logado. Bienvenido al chat!", Toast.LENGTH_SHORT).show();
                    remitente = user.getEmail();
                    //addDatabaseListener(); // nos aseguramos que se asigna el listener a la referencia de la base de datos
                    addChildEventListener(); // si ya está logado se activa la lista
                } else { // el usuario no está logado, limpiamos todo lo que depende de la base de datos
                    remitente = "ANONIMO";
                    clearDatabase();
                    // crear un intent de acceso
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .setTheme(R.style.AppTheme)
                                    .build(), RC_SIGN_IN);
                }}};
        //mFirebaseAuth.addAuthStateListener(mAuthStateListener);


        /*DATABASE*/
        datos = new ArrayList<Mensaje>();

        adapter = new MensajeAdaptador(datos);
        llm = new LinearLayoutManager(this);
        rvMensajes.setLayoutManager(llm);
        rvMensajes.setAdapter(adapter);
        rvMensajes.setItemAnimator(new DefaultItemAnimator());

        dbR = FirebaseDatabase.getInstance().getReference().child("mensaje");

        //addChildEventListener();

        /*STORAGE*/
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFotoStorageRef = mFirebaseStorage.getReference().child("Fotos"); //Nos posicionamos en la carpeta Fotos

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Logado", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Logeo cancelado", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_ADJ) {
            if (resultCode == RESULT_OK) {
                Uri selectedUri = data.getData();
                StorageReference fotoRef = mFotoStorageRef.child(selectedUri.getLastPathSegment());
                UploadTask ut = fotoRef.putFile(selectedUri);
                ut.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Mensaje fm = new Mensaje(null, remitente, uri.toString());
                                dbR.push().setValue(fm);
                            }
                        });
                    }
                });
            }
        }
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

    public void enviarFoto(View v) {
        /*abrirá un selector de archivos para ayudarnos a elegir entre cualquier imagen JPEG almacenada localmente en el dispositivo */
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete la acción usando"), RC_PHOTO_ADJ);

    }

    public void enviar(View v) {
        Mensaje msj = new Mensaje(etMensaje.getText().toString(), remitente, null);
        String clave = dbR.push().getKey();
        dbR.child(clave).setValue(msj);
        //dbR.push().setValue(msj);

        etMensaje.setText("");
    }

    public void aniadir(View v) {
        Mensaje msj = new Mensaje("Bienvenido a Firebase", remitente, null);
        dbR.child("Bienvenida").setValue(msj);
    }

    public void modificar(View v) {
        Map<String, Object> mapa = new HashMap<String, Object>();
        mapa.put("Bienvenida/remitente", null);                 //accede a la propiedad remitente de la clave Bienvenida
        mapa.put("Despedida", new Mensaje("Hasta pronto!", null, null));
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
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        clearDatabase();
    }

    private void clearDatabase() {
        if(cel != null) {
            dbR.removeEventListener(cel);
            cel = null;
        }
        adapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //addChildEventListener();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_salir, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miSalir:
                crearDialogoSalir().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Dialog crearDialogoSalir() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.dialog_salir));
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Auth
                AuthUI.getInstance().signOut(MainActivity.this);
                finish();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
