package br.edu.portal.service;

import br.edu.portal.dao.AlocacaoDAO;
import br.edu.portal.dao.UsuarioDAO;
import br.edu.portal.model.Aula;
import br.edu.portal.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Service: autentica e aplica regras de acesso por perfil.
@Service
public class PortalService {
    private final UsuarioDAO usuarioDAO;
    private final AlocacaoDAO alocacaoDAO;
    private final BCryptPasswordEncoder senhaEncoder = new BCryptPasswordEncoder();

    public PortalService(UsuarioDAO usuarioDAO, AlocacaoDAO alocacaoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.alocacaoDAO = alocacaoDAO;
    }

    public Usuario autenticar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            return null;
        }
        Usuario usuario = usuarioDAO.buscarPorLogin(login.trim());
        if (usuario == null || !senhaEncoder.matches(senha, usuario.getSenhaHash())) {
            return null;
        }
        // Não guarda hash de senha na sessão do navegador.
        return new Usuario(usuario.getId(), usuario.getNome(), null,
                usuario.getPerfil(), usuario.getTurma());
    }

    public List<Aula> aulasDoAluno(Usuario usuario, int diaSemana) {
        if (!"ALUNO".equals(usuario.getPerfil())) {
            throw new IllegalArgumentException("Perfil não autorizado");
        }
        return alocacaoDAO.buscarDoAluno(usuario.getTurma(), diaSemana);
    }

    public List<Aula> aulasDoProfessor(Usuario usuario) {
        if (!"PROFESSOR".equals(usuario.getPerfil())) {
            throw new IllegalArgumentException("Perfil não autorizado");
        }
        return alocacaoDAO.buscarDoProfessor(usuario.getId());
    }
}
