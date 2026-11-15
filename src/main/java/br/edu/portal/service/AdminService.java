package br.edu.portal.service;

import br.edu.portal.dao.AdminDAO;
import br.edu.portal.dao.AlocacaoDAO;
import br.edu.portal.model.Aula;
import br.edu.portal.model.Disciplina;
import br.edu.portal.model.Opcao;
import br.edu.portal.model.Sala;
import br.edu.portal.model.Turma;
import br.edu.portal.model.Usuario;
import br.edu.portal.model.UsuarioAdministrado;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

// Regras de autorização, validação e cadastro de alocações.
@Service
public class AdminService {
    private final AdminDAO adminDAO;
    private final AlocacaoDAO alocacaoDAO;
    private final BCryptPasswordEncoder senhaEncoder = new BCryptPasswordEncoder();

    public AdminService(AdminDAO adminDAO, AlocacaoDAO alocacaoDAO) {
        this.adminDAO = adminDAO;
        this.alocacaoDAO = alocacaoDAO;
    }

    private void exigirAdmin(Usuario usuario) {
        if (usuario == null || !"ADMIN".equals(usuario.getPerfil())) {
            throw new SecurityException("Acesso restrito à coordenação.");
        }
    }

    public List<Opcao> professores(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarProfessores();
    }

    public List<Opcao> disciplinas(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarDisciplinas();
    }

    public List<Opcao> salas(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarSalas();
    }

    public List<Turma> turmas(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarTurmas();
    }

    public List<UsuarioAdministrado> alunos(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarUsuarios("ALUNO");
    }

    public List<UsuarioAdministrado> professoresCadastrados(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarUsuarios("PROFESSOR");
    }

    public List<Sala> salasCadastradas(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarSalasCadastradas();
    }

    public List<Disciplina> disciplinasCadastradas(Usuario usuario) {
        exigirAdmin(usuario);
        return adminDAO.listarDisciplinasCadastradas();
    }

    public List<Aula> alocacoes(Usuario usuario) {
        exigirAdmin(usuario);
        return alocacaoDAO.buscarTodas();
    }

    @Transactional
    public void cadastrar(Usuario usuario, Integer disciplinaId, Integer professorId,
                          String turma, Integer salaId, Integer diaSemana,
                          String horaInicio, String horaFim) {
        exigirAdmin(usuario);

        if (disciplinaId == null || professorId == null || salaId == null || diaSemana == null
                || turma == null || turma.isBlank() || horaInicio == null || horaFim == null) {
            throw new IllegalArgumentException("Preencha todos os campos da alocação.");
        }
        if (diaSemana < 1 || diaSemana > 5) {
            throw new IllegalArgumentException("Selecione um dia de segunda a sexta-feira.");
        }

        String turmaValidada = turma.trim().toUpperCase(Locale.ROOT);
        LocalTime inicio;
        LocalTime fim;
        try {
            // O formulário utiliza HH:mm; valores malformados são rejeitados.
            if (!horaInicio.matches("\\d{2}:\\d{2}") || !horaFim.matches("\\d{2}:\\d{2}")) {
                throw new DateTimeParseException("Formato inválido", horaInicio, 0);
            }
            inicio = LocalTime.parse(horaInicio);
            fim = LocalTime.parse(horaFim);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Informe horários válidos no formato HH:mm.");
        }
        if (!inicio.isBefore(fim)) {
            throw new IllegalArgumentException("O horário final deve ser posterior ao inicial.");
        }

        // Um único bloqueio serializa os cadastros feitos pelo portal antes da checagem de conflitos.
        adminDAO.bloquearCadastros();
        if (!adminDAO.professorExiste(professorId) || !adminDAO.disciplinaExiste(disciplinaId)
                || !adminDAO.salaExiste(salaId) || !adminDAO.turmaExiste(turmaValidada)) {
            throw new IllegalArgumentException("Professor, disciplina, turma ou sala inválidos.");
        }
        if (adminDAO.possuiConflito(diaSemana, inicio, fim, salaId, professorId, turmaValidada)) {
            throw new IllegalArgumentException("Conflito de horário: sala, professor ou turma já ocupados nesse período.");
        }
        adminDAO.inserir(disciplinaId, professorId, turmaValidada, salaId, diaSemana, inicio, fim);
    }

    @Transactional
    public void excluir(Usuario usuario, Integer alocacaoId) {
        exigirAdmin(usuario);
        if (alocacaoId == null || alocacaoId <= 0) {
            throw new IllegalArgumentException("Alocação inválida.");
        }
        if (!adminDAO.excluirAlocacao(alocacaoId)) {
            throw new IllegalArgumentException("A alocação não existe mais ou já foi excluída.");
        }
    }

    @Transactional
    public void cadastrarTurma(Usuario usuario, String codigo, String nome, String curso) {
        exigirAdmin(usuario);
        String codigoValidado = textoObrigatorio(codigo, "Código da turma", 10)
                .toUpperCase(Locale.ROOT);
        String nomeValidado = textoObrigatorio(nome, "Nome da turma", 100);
        String cursoValidado = textoOpcional(curso, "Curso", 100);
        if (!codigoValidado.matches("[A-Z0-9_-]{2,10}")) {
            throw new IllegalArgumentException(
                    "O código deve ter de 2 a 10 letras, números, hífen ou sublinhado.");
        }

        adminDAO.bloquearCadastros();
        if (adminDAO.turmaExiste(codigoValidado)) {
            throw new IllegalArgumentException("Já existe uma turma com esse código.");
        }
        adminDAO.inserirTurma(codigoValidado, nomeValidado, cursoValidado);
    }

    @Transactional
    public void cadastrarProfessor(Usuario usuario, String nome, String login, String senha) {
        exigirAdmin(usuario);
        cadastrarUsuario(nome, login, senha, "PROFESSOR", null);
    }

    @Transactional
    public void cadastrarAluno(Usuario usuario, String nome, String login,
                               String senha, String turma) {
        exigirAdmin(usuario);
        String turmaValidada = textoObrigatorio(turma, "Turma", 10).toUpperCase(Locale.ROOT);
        cadastrarUsuario(nome, login, senha, "ALUNO", turmaValidada);
    }

    @Transactional
    public void cadastrarSala(Usuario usuario, String bloco, String numero, String tipo) {
        exigirAdmin(usuario);
        String blocoValidado = textoObrigatorio(bloco, "Bloco", 10).toUpperCase(Locale.ROOT);
        String numeroValidado = textoObrigatorio(numero, "Número da sala", 10).toUpperCase(Locale.ROOT);
        String tipoValidado = textoObrigatorio(tipo, "Tipo de ambiente", 20).toUpperCase(Locale.ROOT);
        if (!"TEORICA".equals(tipoValidado) && !"LABORATORIO".equals(tipoValidado)) {
            throw new IllegalArgumentException("Selecione um tipo de ambiente válido.");
        }

        adminDAO.bloquearCadastros();
        if (adminDAO.salaExiste(blocoValidado, numeroValidado)) {
            throw new IllegalArgumentException("Já existe um ambiente nesse bloco e número.");
        }
        adminDAO.inserirSala(blocoValidado, numeroValidado, tipoValidado);
    }

    @Transactional
    public void cadastrarDisciplina(Usuario usuario, String nome) {
        exigirAdmin(usuario);
        String nomeValidado = textoObrigatorio(nome, "Nome da disciplina", 100);

        adminDAO.bloquearCadastros();
        if (adminDAO.disciplinaExiste(nomeValidado)) {
            throw new IllegalArgumentException("Já existe uma disciplina com esse nome.");
        }
        adminDAO.inserirDisciplina(nomeValidado);
    }

    private void cadastrarUsuario(String nome, String login, String senha,
                                  String perfil, String turma) {
        String nomeValidado = textoObrigatorio(nome, "Nome completo", 100);
        String loginValidado = textoObrigatorio(login, "Login", 40).toLowerCase(Locale.ROOT);
        if (!loginValidado.matches("[a-z0-9._-]{3,40}")) {
            throw new IllegalArgumentException(
                    "O login deve ter de 3 a 40 letras, números, ponto, hífen ou sublinhado.");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Informe uma senha inicial.");
        }
        if (senha.length() < 6) {
            throw new IllegalArgumentException("A senha inicial deve ter pelo menos 6 caracteres.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha inicial deve ter no máximo 72 bytes.");
        }

        adminDAO.bloquearCadastros();
        if (adminDAO.loginExiste(loginValidado)) {
            throw new IllegalArgumentException("Esse login já está em uso.");
        }
        if (turma != null && !adminDAO.turmaExiste(turma)) {
            throw new IllegalArgumentException("Selecione uma turma cadastrada válida.");
        }
        adminDAO.inserirUsuario(nomeValidado, loginValidado, senhaEncoder.encode(senha), perfil, turma);
    }

    private String textoObrigatorio(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        String validado = valor.trim();
        if (validado.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(campo + " deve ter no máximo " + tamanhoMaximo + " caracteres.");
        }
        return validado;
    }

    private String textoOpcional(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String validado = valor.trim();
        if (validado.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(campo + " deve ter no máximo " + tamanhoMaximo + " caracteres.");
        }
        return validado;
    }
}
