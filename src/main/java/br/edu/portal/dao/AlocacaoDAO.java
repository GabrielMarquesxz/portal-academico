package br.edu.portal.dao;

import br.edu.portal.model.Aula;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

// DAO: usa JOIN para associar disciplina, sala, alocação e professor.
@Repository
public class AlocacaoDAO {
    private final JdbcTemplate jdbc;

    private static final String CONSULTA = """
        SELECT d.nome AS disciplina, a.dia_semana,
               DATE_FORMAT(a.hora_inicio, '%H:%i') AS inicio,
               DATE_FORMAT(a.hora_fim, '%H:%i') AS fim,
               s.bloco, s.numero, s.tipo AS tipo_sala,
               u.nome AS professor
        FROM alocacoes a
        JOIN disciplinas d ON d.id = a.disciplina_id
        JOIN salas s ON s.id = a.sala_id
        JOIN usuarios u ON u.id = a.professor_id
        """;

    public AlocacaoDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Aula mapear(java.sql.ResultSet rs, int linha) throws java.sql.SQLException {
        return new Aula(
                rs.getString("disciplina"), rs.getInt("dia_semana"),
                rs.getString("inicio"), rs.getString("fim"),
                rs.getString("bloco"), rs.getString("numero"),
                rs.getString("tipo_sala"), rs.getString("professor")
        );
    }

    public List<Aula> buscarDoAluno(String turma, int diaSemana) {
        String sql = CONSULTA + " WHERE a.turma = ? AND a.dia_semana = ? ORDER BY a.hora_inicio";
        return jdbc.query(sql, this::mapear, turma, diaSemana);
    }

    public List<Aula> buscarDoProfessor(int professorId) {
        String sql = CONSULTA + " WHERE a.professor_id = ? ORDER BY a.dia_semana, a.hora_inicio";
        return jdbc.query(sql, this::mapear, professorId);
    }
}
