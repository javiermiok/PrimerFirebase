package com.example.a21752434.primerfirebase.javabeans;

public class Mensaje {

    private String textoMsj;
    private String remitente;
    private String fotoUrl;

    public Mensaje() {
    }

    public Mensaje(String textoMsj, String remitente, String fotoUrl) {
        this.textoMsj = textoMsj;
        this.remitente = remitente;
        this.fotoUrl = fotoUrl;
    }

    public String getTextoMsj() {
        return textoMsj;
    }


    public String getRemitente() {
        return remitente;
    }


    public String getFotoUrl() {
        return fotoUrl;
    }
}
