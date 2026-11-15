package br.edu.portal.bean;

import br.edu.portal.model.Aula;
import br.edu.portal.model.Disciplina;
import br.edu.portal.model.Opcao;
import br.edu.portal.model.Sala;
import br.edu.portal.model.Turma;
import br.edu.portal.model.Usuario;
import br.edu.portal.model.UsuarioAdministrado;
import br.edu.portal.service.AdminService;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.io.Serializable;
import java.util.List;

// Controller da área administrativa: protege a página e recebe o formulário.
@Named
@ViewScoped
public class AdminBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient AdminService adminService;

    private Usuario usuario;
    private List<Opcao> professores = List.of();
    private List<Opcao> disciplinas = List.of();
    private List<Opcao> salas = List.of();
    private List<Turma> turmas = List.of();
    private List<UsuarioAdministrado> alunos = List.of();
    private List<UsuarioAdministrado> professoresCadastrados = List.of();
    private List<Sala> salasCadastradas = List.of();
    private List<Disciplina> disciplinasCadastradas = List.of();
    private List<Aula> alocacoes = List.of();

    private Integer professorId;
    private Integer disciplinaId;
    private Integer salaId;
    private Integer diaSemana;
    private String turma;
    private String horaInicio = "08:00";
    private String horaFim = "09:40";

    private String novaTurmaCodigo;
    private String novaTurmaNome;
    private String novaTurmaCurso;
    private String novoProfessorNome;
    private String novoProfessorLogin;
    private String novoProfessorSenha;
    private String novoAlunoNome;
    private String novoAlunoLogin;
    private String novoAlunoSenha;
    private String novoAlunoTurma;
    private String novaSalaBloco;
    private String novaSalaNumero;
    private String novaSalaTipo;
    private String novaDisciplinaNome;
    private String sucesso;
    private String erro;

    public String abrir() {
        ExternalContext contexto = FacesContext.getCurrentInstance().getExternalContext();
        Object autenticado = contexto.getSessionMap().get("usuario");
        if (!(autenticado instanceof Usuario logado)) {
            return "/login.xhtml?faces-redirect=true";
        }
        if (!"ADMIN".equals(logado.getPerfil())) {
            return ("ALUNO".equals(logado.getPerfil()) ? "/aluno.xhtml" : "/professor.xhtml")
                    + "?faces-redirect=true";
        }
        usuario = logado;
        try {
            carregarDados();
        } catch (DataAccessException ex) {
            erro = "Não foi possível carregar os dados do painel administrativo.";
        }
        return null;
    }

    public String salvar() {
        sucesso = null;
        erro = null;
        Object autenticado = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("usuario");
        if (!(autenticado instanceof Usuario logado) || !"ADMIN".equals(logado.getPerfil())) {
            return "/login.xhtml?faces-redirect=true";
        }
        usuario = logado;
        try {
            adminService.cadastrar(usuario, disciplinaId, professorId, turma, salaId,
                    diaSemana, horaInicio, horaFim);
            alocacoes = adminService.alocacoes(usuario);
            professorId = null;
            disciplinaId = null;
            salaId = null;
            diaSemana = null;
            turma = null;
            horaInicio = "08:00";
            horaFim = "09:40";
            sucesso = "Alocação cadastrada com sucesso. Ela já está disponível nos painéis.";
        } catch (IllegalArgumentException ex) {
            erro = ex.getMessage();
        } catch (DataAccessException ex) {
            erro = "Não foi possível salvar a alocação no banco de dados.";
        }
        return null;
    }

    public String excluir(Integer alocacaoId) {
        sucesso = null;
        erro = null;
        Object autenticado = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("usuario");
        if (!(autenticado instanceof Usuario logado) || !"ADMIN".equals(logado.getPerfil())) {
            return "/login.xhtml?faces-redirect=true";
        }
        usuario = logado;
        try {
            adminService.excluir(usuario, alocacaoId);
            alocacoes = adminService.alocacoes(usuario);
            sucesso = "Alocação excluída com sucesso.";
        } catch (IllegalArgumentException ex) {
            erro = ex.getMessage();
        } catch (DataAccessException ex) {
            erro = "Não foi possível excluir a alocação do banco de dados.";
        }
        return null;
    }

    public String salvarTurma() {
        limparMensagens();
        if (!recuperarAdminDaSessao()) return redirecionarLogin();
        try {
            adminService.cadastrarTurma(usuario, novaTurmaCodigo, novaTurmaNome, novaTurmaCurso);
            novaTurmaCodigo = null;
            novaTurmaNome = null;
            novaTurmaCurso = null;
            carregarDados();
            sucesso = "Turma cadastrada com sucesso.";
        } catch (IllegalArgumentException ex) {
            erro = ex.getMessage();
        } catch (DataAccessException ex) {
            erro = "Não foi possível cadastrar a turma no banco de dados.";
        }
        return null;
    }

    public String salvarProfessor() {
        limparMensagens();
        if (!recuperarAdminDaSessao()) return redirecionarLogin();
        try {
            adminService.cadastrarProfessor(usuario, novoProfessorNome,
                    novoProfessorLogin, novoProfessorSenha);
            novoProfessorNome = null;
            novoProfessorLogin = null;
            novoProfessorSenha = null;
            carregarDados();
            sucesso = "Professor cadastrado com sucesso e disponível para alocações.";
        } catch (IllegalArgumentException ex) {
            erro = ex.getMessage();
        } catch (DataAccessException ex) {
            erro = "Não foi possível cadastrar o professor no banco de dados.";
        }
        return null;
    }

    public String salvarAluno() {
        limparMensagens();
        if (!recuperarAdminDaSessao()) return redirecionarLogin();
        try {
            adminService.cadastrarAluno(usuario, novoAlunoNome, novoAlunoLogin,
                    novoAlunoSenha, novoAlunoTurma);
            novoAlunoNome = null;
            novoAlunoLogin = null;
            novoAlunoSenha = null;
            novoAlunoTurma = null;
            carregarDados();
            sucesso = "Aluno cadastrado com sucesso.";
        } catch (IllegalArgumentException ex) {
            erro = ex.getMessage();
        } catch (DataAccessException ex) {
            erro = "Não foi possível cadastrar o aluno no banco de dados.";
        }
        return null;
    }

    public String salvarSala() {
        limparMensagens();
        if (!recuperarAdminDaSessao()) return redirecionarLogin();
        try {
            adminService.cadastrarSala(usuario, novaSalaBloco, novaSalaNumero, novaSalaTipo);
            novaSalaBloco = null;
            novaSalaNumero = null;
            novaSalaTipo = null;
            carregarDados();
            sucesso = "Sala ou laboratório cadastrado com sucesso.";
        } catch (IllegalArgumentException ex) {
            erro = ex.getMessage();
        } catch (DataAccessException ex) {
            erro = "Não foi possível cadastrar o ambiente no banco de dados.";
        }
        return null;
    }

    public String salvarDisciplina() {
        limparMensagens();
        if (!recuperarAdminDaSessao()) return redirecionarLogin();
        try {
            adminService.cadastrarDisciplina(usuario, novaDisciplinaNome);
            novaDisciplinaNome = null;
            carregarDados();
            sucesso = "Disciplina cadastrada com sucesso e disponível para alocações.";
        } catch (IllegalArgumentException ex) {
            erro = ex.getMessage();
        } catch (DataAccessException ex) {
            erro = "Não foi possível cadastrar a disciplina no banco de dados.";
        }
        return null;
    }

    private void carregarDados() {
        professores = adminService.professores(usuario);
        disciplinas = adminService.disciplinas(usuario);
        salas = adminService.salas(usuario);
        turmas = adminService.turmas(usuario);
        alunos = adminService.alunos(usuario);
        professoresCadastrados = adminService.professoresCadastrados(usuario);
        salasCadastradas = adminService.salasCadastradas(usuario);
        disciplinasCadastradas = adminService.disciplinasCadastradas(usuario);
        alocacoes = adminService.alocacoes(usuario);
    }

    private boolean recuperarAdminDaSessao() {
        Object autenticado = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("usuario");
        if (autenticado instanceof Usuario logado && "ADMIN".equals(logado.getPerfil())) {
            usuario = logado;
            return true;
        }
        return false;
    }

    private void limparMensagens() {
        sucesso = null;
        erro = null;
    }

    private String redirecionarLogin() {
        return "/login.xhtml?faces-redirect=true";
    }

    public String sair() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public Usuario getUsuario() { return usuario; }
    public List<Opcao> getProfessores() { return professores; }
    public List<Opcao> getDisciplinas() { return disciplinas; }
    public List<Opcao> getSalas() { return salas; }
    public List<Turma> getTurmas() { return turmas; }
    public List<UsuarioAdministrado> getAlunos() { return alunos; }
    public List<UsuarioAdministrado> getProfessoresCadastrados() { return professoresCadastrados; }
    public List<Sala> getSalasCadastradas() { return salasCadastradas; }
    public List<Disciplina> getDisciplinasCadastradas() { return disciplinasCadastradas; }
    public List<Aula> getAlocacoes() { return alocacoes; }
    public int getTotalAlocacoes() { return alocacoes.size(); }
    public long getTotalLaboratorios() {
        return alocacoes.stream().filter(a -> "LABORATORIO".equals(a.getTipoSala())).count();
    }
    public long getTotalProfessores() {
        return alocacoes.stream().map(Aula::getProfessor).distinct().count();
    }
    public Integer getProfessorId() { return professorId; }
    public void setProfessorId(Integer professorId) { this.professorId = professorId; }
    public Integer getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(Integer disciplinaId) { this.disciplinaId = disciplinaId; }
    public Integer getSalaId() { return salaId; }
    public void setSalaId(Integer salaId) { this.salaId = salaId; }
    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
    public String getTurma() { return turma; }
    public void setTurma(String turma) { this.turma = turma; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }
    public String getNovaTurmaCodigo() { return novaTurmaCodigo; }
    public void setNovaTurmaCodigo(String novaTurmaCodigo) { this.novaTurmaCodigo = novaTurmaCodigo; }
    public String getNovaTurmaNome() { return novaTurmaNome; }
    public void setNovaTurmaNome(String novaTurmaNome) { this.novaTurmaNome = novaTurmaNome; }
    public String getNovaTurmaCurso() { return novaTurmaCurso; }
    public void setNovaTurmaCurso(String novaTurmaCurso) { this.novaTurmaCurso = novaTurmaCurso; }
    public String getNovoProfessorNome() { return novoProfessorNome; }
    public void setNovoProfessorNome(String novoProfessorNome) { this.novoProfessorNome = novoProfessorNome; }
    public String getNovoProfessorLogin() { return novoProfessorLogin; }
    public void setNovoProfessorLogin(String novoProfessorLogin) { this.novoProfessorLogin = novoProfessorLogin; }
    public String getNovoProfessorSenha() { return novoProfessorSenha; }
    public void setNovoProfessorSenha(String novoProfessorSenha) { this.novoProfessorSenha = novoProfessorSenha; }
    public String getNovoAlunoNome() { return novoAlunoNome; }
    public void setNovoAlunoNome(String novoAlunoNome) { this.novoAlunoNome = novoAlunoNome; }
    public String getNovoAlunoLogin() { return novoAlunoLogin; }
    public void setNovoAlunoLogin(String novoAlunoLogin) { this.novoAlunoLogin = novoAlunoLogin; }
    public String getNovoAlunoSenha() { return novoAlunoSenha; }
    public void setNovoAlunoSenha(String novoAlunoSenha) { this.novoAlunoSenha = novoAlunoSenha; }
    public String getNovoAlunoTurma() { return novoAlunoTurma; }
    public void setNovoAlunoTurma(String novoAlunoTurma) { this.novoAlunoTurma = novoAlunoTurma; }
    public String getNovaSalaBloco() { return novaSalaBloco; }
    public void setNovaSalaBloco(String novaSalaBloco) { this.novaSalaBloco = novaSalaBloco; }
    public String getNovaSalaNumero() { return novaSalaNumero; }
    public void setNovaSalaNumero(String novaSalaNumero) { this.novaSalaNumero = novaSalaNumero; }
    public String getNovaSalaTipo() { return novaSalaTipo; }
    public void setNovaSalaTipo(String novaSalaTipo) { this.novaSalaTipo = novaSalaTipo; }
    public String getNovaDisciplinaNome() { return novaDisciplinaNome; }
    public void setNovaDisciplinaNome(String novaDisciplinaNome) { this.novaDisciplinaNome = novaDisciplinaNome; }
    public String getSucesso() { return sucesso; }
    public String getErro() { return erro; }
}
