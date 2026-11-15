package br.edu.portal.bean;

import br.edu.portal.model.Usuario;
import br.edu.portal.service.PortalService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.io.Serializable;

// Controller JSF: recebe usuário/senha, chama o Service e escolhe a página.
@Named
@ViewScoped
public class LoginBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient PortalService portalService;

    private String login;
    private String senha;

    public String entrar() {
        try {
            Usuario usuario = portalService.autenticar(login, senha);
            if (usuario == null) {
                mensagem("Usuário ou senha inválidos.");
                return null;
            }

            ExternalContext contexto = FacesContext.getCurrentInstance().getExternalContext();
            contexto.invalidateSession(); // Cria uma sessão nova após a autenticação.
            contexto.getSessionMap().put("usuario", usuario);
            return switch (usuario.getPerfil()) {
                case "ALUNO" -> "/aluno.xhtml?faces-redirect=true";
                case "PROFESSOR" -> "/professor.xhtml?faces-redirect=true";
                case "ADMIN" -> "/admin.xhtml?faces-redirect=true";
                default -> "/login.xhtml?faces-redirect=true";
            };
        } catch (DataAccessException ex) {
            mensagem("Não foi possível consultar o banco de dados.");
            return null;
        }
    }

    private void mensagem(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
