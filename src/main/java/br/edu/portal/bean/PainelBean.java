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
import java.util.List;

// Controller JSF: protege as páginas e busca somente as aulas autorizadas.
@Named
@ViewScoped
public class PainelBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient PortalService portalService;

    private Usuario usuario;
    private List<Aula> aulas = List.of();
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
            return ("ALUNO".equals(logado.getPerfil()) ? "/aluno.xhtml" : "/professor.xhtml")
                    + "?faces-redirect=true";
        }
        usuario = logado;
        try {
            if ("ALUNO".equals(perfilEsperado)) {
                int dia = LocalDate.now(ZoneId.of("America/Sao_Paulo")).getDayOfWeek().getValue();
                aulas = portalService.aulasDoAluno(logado, dia);
            } else {
                aulas = portalService.aulasDoProfessor(logado);
            }
        } catch (DataAccessException ex) {
            erro = "Não foi possível carregar as aulas do banco de dados.";
        }
        return null;
    }

    public String sair() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public Usuario getUsuario() { return usuario; }
    public List<Aula> getAulas() { return aulas; }
    public String getErro() { return erro; }
}
