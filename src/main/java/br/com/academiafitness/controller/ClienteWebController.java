package br.com.academiafitness.controller;

import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.filtro.ClienteFiltro;
import br.com.academiafitness.filtro.SituacaoCliente;
import br.com.academiafitness.form.ClienteForm;
import br.com.academiafitness.model.Cliente;
import br.com.academiafitness.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/clientes")
public class ClienteWebController {

    private final ClienteService clientes;

    public ClienteWebController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    public String listar(@ModelAttribute("filtro") ClienteFiltro filtro, BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("aviso", "Algum filtro tinha valor inválido e foi ignorado.");
        }
        if (filtro.periodoInvertido()) {
            model.addAttribute("aviso", "A data inicial é posterior à data final; o período foi ignorado.");
            filtro.setCadastroDe(null);
            filtro.setCadastroAte(null);
        }
        List<Cliente> resultado = clientes.pesquisar(filtro);
        model.addAttribute("clientes", resultado);
        model.addAttribute("total", resultado.size());
        model.addAttribute("objetivos", clientes.objetivos());
        model.addAttribute("situacoes", SituacaoCliente.values());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return formulario(model, new ClienteForm(), "Novo cliente", "/clientes");
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ClienteForm form, BindingResult result,
                        Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                clientes.criar(form);
                ra.addFlashAttribute("sucesso", "Cliente cadastrado com sucesso.");
                return "redirect:/clientes";
            } catch (RegraNegocioException ex) {
                result.rejectValue("email", "email.duplicado", ex.getMessage());
            }
        }
        return formulario(model, form, "Novo cliente", "/clientes");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, ClienteForm.from(clientes.buscar(id)), "Editar cliente", "/clientes/" + id);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ClienteForm form,
                            BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                clientes.atualizar(id, form);
                ra.addFlashAttribute("sucesso", "Cliente atualizado com sucesso.");
                return "redirect:/clientes";
            } catch (RegraNegocioException ex) {
                result.rejectValue("email", "email.duplicado", ex.getMessage());
            }
        }
        return formulario(model, form, "Editar cliente", "/clientes/" + id);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            clientes.excluir(id);
            ra.addFlashAttribute("sucesso", "Cliente excluído.");
        } catch (RegraNegocioException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/clientes";
    }

    private String formulario(Model model, ClienteForm form, String titulo, String acao) {
        model.addAttribute("form", form);
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        return "clientes/form";
    }
}
