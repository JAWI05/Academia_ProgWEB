package br.com.academiafitness.form;

import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioForm {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Máximo de 100 caracteres")
    private String nome;

    @NotBlank(message = "Informe o login")
    @Size(min = 3, max = 50, message = "O login deve ter de 3 a 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "Use apenas letras, números, ponto, hífen e sublinhado")
    private String login;

    /** Obrigatória ao criar; ao editar, vazia = manter a senha atual. */
    @Size(min = 6, max = 60, message = "A senha deve ter de 6 a 60 caracteres")
    private String senha;

    @NotNull(message = "Selecione o perfil")
    private Perfil perfil;

    public static UsuarioForm from(Usuario u) {
        UsuarioForm f = new UsuarioForm();
        f.nome = u.getNome();
        f.login = u.getLogin();
        f.perfil = u.getPerfil();
        return f;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
}
