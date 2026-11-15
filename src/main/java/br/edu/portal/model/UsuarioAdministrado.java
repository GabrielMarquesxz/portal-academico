package br.edu.portal.model;

import java.io.Serializable;

// Dados públicos de um aluno ou professor exibidos no painel administrativo.
public class UsuarioAdministrado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nome;
    private final String login;
    private final String turma;

    public UsuarioAdministrado(int id, String nome, String login, String turma) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.turma = turma;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getLogin() { return login; }
    public String getTurma() { return turma; }
}
