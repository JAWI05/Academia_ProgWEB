package br.com.academiafitness.controller;

import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.form.UsuarioForm;
import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    private final UsuarioService usuarios;

    public UsuarioWebController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    /** Campos de texto em branco viram null (senha vazia = manter a atual). */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String listar(Model model, Authentication authentication) {
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("loginLogado", authentication.getName());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return formulario(model, new UsuarioForm(), "Novo usuário", "/usuarios", false);
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult result,
                        Model model, RedirectAttributes ra) {
        if (form.getSenha() == null) {
            result.rejectValue("senha", "senha.obrigatoria", "Informe a senha");
        }
        if (!result.hasErrors()) {
            try {
                usuarios.criar(form);
                ra.addFlashAttribute("sucesso", "Usuário criado com sucesso.");
                return "redirect:/usuarios";
            } catch (RegraNegocioException ex) {
                result.rejectValue("login", "login.duplicado", ex.getMessage());
            }
        }
        return formulario(model, form, "Novo usuário", "/usuarios", false);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, UsuarioForm.from(usuarios.buscar(id)), "Editar usuário", "/usuarios/" + id, true);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") UsuarioForm form,
                            BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                usuarios.atualizar(id, form);
                ra.addFlashAttribute("sucesso", "Usuário atualizado com sucesso.");
                return "redirect:/usuarios";
            } catch (RegraNegocioException ex) {
                result.reject("regra", ex.getMessage());
            }
        }
        return formulario(model, form, "Editar usuário", "/usuarios/" + id, true);
    }

    @PostMapping("/{id}/ativo")
    public String alternarAtivo(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            var usuario = usuarios.alternarAtivo(id, authentication.getName());
            ra.addFlashAttribute("sucesso", usuario.isAtivo() ? "Usuário ativado." : "Usuário desativado.");
        } catch (RegraNegocioException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/usuarios";
    }

    private String formulario(Model model, UsuarioForm form, String titulo, String acao, boolean editando) {
        model.addAttribute("form", form);
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        model.addAttribute("editando", editando);
        model.addAttribute("perfis", Perfil.values());
        return "usuarios/form";
    }
}
