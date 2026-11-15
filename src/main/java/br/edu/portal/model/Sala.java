package br.edu.portal.model;

import java.io.Serializable;

// Sala teórica ou laboratório disponível para alocações.
public class Sala implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String bloco;
    private final String numero;
    private final String tipo;

    public Sala(int id, String bloco, String numero, String tipo) {
        this.id = id;
        this.bloco = bloco;
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public String getBloco() { return bloco; }
    public String getNumero() { return numero; }
    public String getTipo() { return tipo; }
    public String getTipoExibicao() {
        return "LABORATORIO".equals(tipo) ? "Laboratório" : "Teórica";
    }
}
