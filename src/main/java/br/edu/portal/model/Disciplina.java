package br.edu.portal.model;

import java.io.Serializable;

// Disciplina disponível para composição das alocações acadêmicas.
public class Disciplina implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nome;

    public Disciplina(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
}
