package br.com.academiafitness.form;

import br.com.academiafitness.model.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteForm {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Máximo de 100 caracteres")
    private String nome;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    @Size(max = 120, message = "Máximo de 120 caracteres")
    private String email;

    @Size(max = 120, message = "Máximo de 120 caracteres")
    private String objetivo;

    public static ClienteForm from(Cliente c) {
        ClienteForm f = new ClienteForm();
        f.nome = c.getNome();
        f.email = c.getEmail();
        f.objetivo = c.getObjetivo();
        return f;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
}
