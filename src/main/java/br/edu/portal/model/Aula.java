package br.edu.portal.model;

// Model: dados que a tela precisa apresentar sobre uma aula.
public class Aula implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String disciplina;
    private final int diaSemana;
    private final String inicio;
    private final String fim;
    private final String bloco;
    private final String numero;
    private final String tipoSala;
    private final String professor;

    public Aula(String disciplina, int diaSemana, String inicio, String fim,
                String bloco, String numero, String tipoSala, String professor) {
        this.disciplina = disciplina;
        this.diaSemana = diaSemana;
        this.inicio = inicio;
        this.fim = fim;
        this.bloco = bloco;
        this.numero = numero;
        this.tipoSala = tipoSala;
        this.professor = professor;
    }

    public String getDisciplina() { return disciplina; }
    public String getProfessor() { return professor; }
    public String getHorario() { return inicio + " às " + fim; }
    public String getLocal() { return "Bloco " + bloco + " — Sala " + numero; }
    public String getTipoAula() {
        return "LABORATORIO".equals(tipoSala) ? "Prática (laboratório)" : "Teórica";
    }
    public String getDiaTexto() {
        String[] dias = {"", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"};
        return dias[diaSemana];
    }
}
