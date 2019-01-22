package com.example.a21752434.primerfirebase.javabeans;

public class Mensaje {

    private String textoMsj;
    private String remitente;

    public Mensaje() {
    }

    public Mensaje(String textoMsj, String remitente) {
        this.textoMsj = textoMsj;
        this.remitente = remitente;
    }

    public String getTextoMsj() {
        return textoMsj;
    }

    public void setTextoMsj(String textoMsj) {
        this.textoMsj = textoMsj;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }
}
