package br.edu.portal.model;

// Opção de seleção para professores, disciplinas e salas no painel administrativo.
public class Opcao implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String nome;

    public Opcao(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
}
