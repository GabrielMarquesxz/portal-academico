package br.edu.portal.bean;

import br.edu.portal.model.Aula;
import br.edu.portal.model.Usuario;
import br.edu.portal.service.PortalService;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// Controller: sessão, navegação, filtros e dados do painel.
@Named
@ViewScoped
public class PainelBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private transient PortalService portalService;

    private Usuario usuario;
    private List<Aula> aulasSemana = List.of();
    private List<Aula> aulasHoje = List.of();
    private List<Aula> aulas = List.of();
    private int diaSelecionado;
    private int diaAtual;
    private String erro;

    public String abrirAluno() { return abrir("ALUNO"); }
    public String abrirProfessor() { return abrir("PROFESSOR"); }

    private String abrir(String perfilEsperado) {
        ExternalContext contexto = FacesContext.getCurrentInstance().getExternalContext();
        Object autenticado = contexto.getSessionMap().get("usuario");
        if (!(autenticado instanceof Usuario logado)) {
            return "/login.xhtml?faces-redirect=true";
        }
        if (!perfilEsperado.equals(logado.getPerfil())) {
            return switch (logado.getPerfil()) {
                case "ALUNO" -> "/aluno.xhtml?faces-redirect=true";
                case "PROFESSOR" -> "/professor.xhtml?faces-redirect=true";
                case "ADMIN" -> "/admin.xhtml?faces-redirect=true";
                default -> "/login.xhtml?faces-redirect=true";
            };
        }

        usuario = logado;
        diaAtual = LocalDate.now(FUSO).getDayOfWeek().getValue();
        diaSelecionado = "ALUNO".equals(perfilEsperado) ? diaAtual : 0;
        erro = null;
        try {
            aulasSemana = "ALUNO".equals(perfilEsperado)
                    ? portalService.aulasDaSemanaDoAluno(logado)
                    : portalService.aulasDoProfessor(logado);
            aulasHoje = aulasSemana.stream().filter(a -> a.getDiaSemana() == diaAtual).toList();
            aplicarFiltro();
        } catch (DataAccessException ex) {
            aulasSemana = List.of();
            aulasHoje = List.of();
            aulas = List.of();
            erro = "Não foi possível carregar as aulas do banco de dados.";
        }
        return null;
    }

    // O filtro funciona no JSF sem novas consultas SQL: todos os dados já são do usuário autenticado.
    public String filtrarDia(int dia) {
        if (dia >= 0 && dia <= 7) {
            diaSelecionado = dia;
            aplicarFiltro();
        }
        return null;
    }

    private void aplicarFiltro() {
        aulas = diaSelecionado == 0
                ? aulasSemana
                : aulasSemana.stream().filter(a -> a.getDiaSemana() == diaSelecionado).toList();
    }

    public String sair() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public Usuario getUsuario() { return usuario; }
    public List<Aula> getAulas() { return aulas; }
    public List<Aula> getAulasHoje() { return aulasHoje; }
    public String getErro() { return erro; }
    public int getDiaSelecionado() { return diaSelecionado; }
    public int getDiaAtual() { return diaAtual; }
    public int getTotalAulasSemana() { return aulasSemana.size(); }
    public int getTotalAulasHoje() { return aulasHoje.size(); }
    public long getTotalLaboratorios() {
        return aulasSemana.stream().filter(a -> "LABORATORIO".equals(a.getTipoSala())).count();
    }
    public long getTotalDiasComAula() {
        return aulasSemana.stream().map(Aula::getDiaSemana).distinct().count();
    }
    public String getDataHoje() {
        return LocalDate.now(FUSO).format(
                DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR")));
    }
    public String getPeriodoSelecionado() {
        if (diaSelecionado == 0) return "Todas as aulas da semana";
        String[] dias = {"", "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira",
                "sexta-feira", "sábado", "domingo"};
        return "Aulas de " + dias[diaSelecionado];
    }
}
