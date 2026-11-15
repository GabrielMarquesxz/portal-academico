package br.edu.portal.dao;

import br.edu.portal.model.Disciplina;
import br.edu.portal.model.Opcao;
import br.edu.portal.model.Sala;
import br.edu.portal.model.Turma;
import br.edu.portal.model.UsuarioAdministrado;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

// DAO: somente acesso aos dados do cadastro administrativo.
@Repository
public class AdminDAO {
    private final JdbcTemplate jdbc;

    public AdminDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Opcao> listarProfessores() {
        return jdbc.query("""
                SELECT u.id, u.nome FROM usuarios u
                JOIN perfis p ON p.id = u.perfil_id
                WHERE p.nome = 'PROFESSOR' ORDER BY u.nome
                """, (rs, linha) -> new Opcao(rs.getInt("id"), rs.getString("nome")));
    }

    public List<Opcao> listarDisciplinas() {
        return jdbc.query("SELECT id, nome FROM disciplinas ORDER BY nome",
                (rs, linha) -> new Opcao(rs.getInt("id"), rs.getString("nome")));
    }

    public List<Opcao> listarSalas() {
        return jdbc.query("""
                SELECT id, CONCAT('Bloco ', bloco, ' — Sala ', numero, ' (',
                        CASE WHEN tipo = 'LABORATORIO' THEN 'Laboratório' ELSE 'Teórica' END,
                        ')') AS nome
                FROM salas ORDER BY bloco, numero
                """, (rs, linha) -> new Opcao(rs.getInt("id"), rs.getString("nome")));
    }

    public List<Turma> listarTurmas() {
        return jdbc.query("SELECT codigo, nome, curso FROM turmas ORDER BY codigo",
                (rs, linha) -> new Turma(rs.getString("codigo"), rs.getString("nome"),
                        rs.getString("curso")));
    }

    public List<UsuarioAdministrado> listarUsuarios(String perfil) {
        return jdbc.query("""
                SELECT u.id, u.nome, u.login, u.turma
                FROM usuarios u
                JOIN perfis p ON p.id = u.perfil_id
                WHERE p.nome = ?
                ORDER BY u.nome
                """, (rs, linha) -> new UsuarioAdministrado(
                        rs.getInt("id"), rs.getString("nome"), rs.getString("login"),
                        rs.getString("turma")), perfil);
    }

    public List<Sala> listarSalasCadastradas() {
        return jdbc.query("SELECT id, bloco, numero, tipo FROM salas ORDER BY bloco, numero",
                (rs, linha) -> new Sala(rs.getInt("id"), rs.getString("bloco"),
                        rs.getString("numero"), rs.getString("tipo")));
    }

    public List<Disciplina> listarDisciplinasCadastradas() {
        return jdbc.query("SELECT id, nome FROM disciplinas ORDER BY nome",
                (rs, linha) -> new Disciplina(rs.getInt("id"), rs.getString("nome")));
    }

    public boolean professorExiste(int id) {
        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM usuarios u
                JOIN perfis p ON p.id = u.perfil_id
                WHERE u.id = ? AND p.nome = 'PROFESSOR'
                """, Integer.class, id);
        return total != null && total > 0;
    }

    public boolean disciplinaExiste(int id) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM disciplinas WHERE id = ?", Integer.class, id);
        return total != null && total > 0;
    }

    public boolean salaExiste(int id) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM salas WHERE id = ?", Integer.class, id);
        return total != null && total > 0;
    }

    public boolean turmaExiste(String turma) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM turmas WHERE codigo = ?",
                Integer.class, turma);
        return total != null && total > 0;
    }

    public boolean loginExiste(String login) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE LOWER(login) = LOWER(?)", Integer.class, login);
        return total != null && total > 0;
    }

    public boolean salaExiste(String bloco, String numero) {
        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM salas
                WHERE LOWER(bloco) = LOWER(?) AND LOWER(numero) = LOWER(?)
                """, Integer.class, bloco, numero);
        return total != null && total > 0;
    }

    public boolean disciplinaExiste(String nome) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM disciplinas WHERE LOWER(nome) = LOWER(?)", Integer.class, nome);
        return total != null && total > 0;
    }

    // A transação do Service mantém este bloqueio até concluir o cadastro.
    // Isso evita que dois administradores cadastrem horários conflitantes simultaneamente.
    public void bloquearCadastros() {
        jdbc.queryForObject("SELECT id FROM perfis WHERE nome = 'ADMIN' FOR UPDATE", Integer.class);
    }

    public boolean possuiConflito(int dia, LocalTime inicio, LocalTime fim,
                                  int salaId, int professorId, String turma) {
        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM alocacoes
                WHERE dia_semana = ? AND hora_inicio < ? AND hora_fim > ?
                  AND (sala_id = ? OR professor_id = ? OR turma = ?)
                """, Integer.class,
                dia, Time.valueOf(fim), Time.valueOf(inicio), salaId, professorId, turma);
        return total != null && total > 0;
    }

    public void inserir(int disciplinaId, int professorId, String turma, int salaId,
                        int dia, LocalTime inicio, LocalTime fim) {
        jdbc.update("""
                INSERT INTO alocacoes
                    (disciplina_id, professor_id, turma, sala_id, dia_semana, hora_inicio, hora_fim)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, disciplinaId, professorId, turma, salaId, dia,
                Time.valueOf(inicio), Time.valueOf(fim));
    }

    public boolean excluirAlocacao(int id) {
        return jdbc.update("DELETE FROM alocacoes WHERE id = ?", id) == 1;
    }

    public void inserirTurma(String codigo, String nome, String curso) {
        jdbc.update("INSERT INTO turmas (codigo, nome, curso) VALUES (?, ?, ?)",
                codigo, nome, curso);
    }

    public void inserirUsuario(String nome, String login, String senhaHash,
                               String perfil, String turma) {
        jdbc.update("""
                INSERT INTO usuarios (id, nome, login, senha_hash, perfil_id, turma)
                SELECT ids.proximo_id, ?, ?, ?, p.id, ?
                FROM perfis p
                CROSS JOIN (
                    SELECT COALESCE(MAX(id), 0) + 1 AS proximo_id FROM usuarios
                ) ids
                WHERE p.nome = ?
                """, nome, login, senhaHash, turma, perfil);
    }

    public void inserirSala(String bloco, String numero, String tipo) {
        jdbc.update("""
                INSERT INTO salas (id, bloco, numero, tipo)
                SELECT COALESCE(MAX(id), 0) + 1, ?, ?, ? FROM salas
                """, bloco, numero, tipo);
    }

    public void inserirDisciplina(String nome) {
        jdbc.update("""
                INSERT INTO disciplinas (id, nome)
                SELECT COALESCE(MAX(id), 0) + 1, ? FROM disciplinas
                """, nome);
    }
}
