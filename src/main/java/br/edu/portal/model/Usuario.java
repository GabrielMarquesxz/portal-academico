package br.edu.portal.model;

import java.io.Serializable;

// Model: representa um usuário que veio do banco de dados.
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nome;
    private final String senhaHash;
    private final String perfil;
    private final String turma;

    public Usuario(int id, String nome, String senhaHash, String perfil, String turma) {
        this.id = id;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.turma = turma;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getSenhaHash() { return senhaHash; }
    public String getPerfil() { return perfil; }
    public String getTurma() { return turma; }
}
