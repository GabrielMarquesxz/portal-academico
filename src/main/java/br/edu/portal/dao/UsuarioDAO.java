package br.edu.portal.dao;

import br.edu.portal.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// DAO: faz SELECT no banco; a senha nunca é verificada dentro da tela.
@Repository
public class UsuarioDAO {
    private final JdbcTemplate jdbc;

    public UsuarioDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Usuario buscarPorLogin(String login) {
        String sql = """
            SELECT u.id, u.nome, u.senha_hash, p.nome AS perfil, u.turma
            FROM usuarios u
            JOIN perfis p ON p.id = u.perfil_id
            WHERE u.login = ?
            """;

        return jdbc.query(sql, (rs, linha) -> new Usuario(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("senha_hash"),
                rs.getString("perfil"),
                rs.getString("turma")
        ), login).stream().findFirst().orElse(null);
    }
}
