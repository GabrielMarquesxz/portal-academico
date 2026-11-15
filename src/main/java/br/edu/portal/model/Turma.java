package br.edu.portal.model;

import java.io.Serializable;

// Turma cadastrada pela coordenação e reutilizada por alunos e alocações.
public class Turma implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String nome;
    private final String curso;

    public Turma(String codigo, String nome, String curso) {
        this.codigo = codigo;
        this.nome = nome;
        this.curso = curso;
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getCurso() { return curso; }
    public String getCursoExibicao() {
        return curso == null || curso.isBlank() ? "Não informado" : curso;
    }
    public String getRotulo() { return codigo + " — " + nome; }
}
