package br.com.academiafitness.controller;

import br.com.academiafitness.dto.AulaResponse;
import br.com.academiafitness.exception.RecursoNaoEncontradoException;
import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.form.AulaForm;
import br.com.academiafitness.model.Cliente;
import br.com.academiafitness.model.StatusAula;
import br.com.academiafitness.service.AulaService;
import br.com.academiafitness.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/aulas")
public class AulaWebController {

    private final AulaService aulas;
    private final CadastroService cadastros;

    public AulaWebController(AulaService aulas, CadastroService cadastros) {
        this.aulas = aulas;
        this.cadastros = cadastros;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusAula status, Model model) {
        model.addAttribute("aulas", aulas.listar(status));
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("todosStatus", StatusAula.values());
        return "aulas/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        AulaResponse aula = aulas.buscar(id);
        Set<Long> jaInscritos = aula.inscritos().stream()
                .map(AulaResponse.ClienteResumo::id).collect(Collectors.toSet());
        List<Cliente> disponiveis = cadastros.clientes().stream()
                .filter(c -> !jaInscritos.contains(c.getId())).toList();
        model.addAttribute("aula", aula);
        model.addAttribute("clientesDisponiveis", disponiveis);
        return "aulas/detalhe";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        return formulario(model, new AulaForm(), "Nova aula", "/aulas");
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") AulaForm form, BindingResult result,
                        Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                AulaResponse criada = aulas.criar(form.toRequest());
                ra.addFlashAttribute("sucesso", "Aula criada com sucesso.");
                return "redirect:/aulas/" + criada.id();
            } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
                result.reject("regra", ex.getMessage());
            }
        }
        return formulario(model, form, "Nova aula", "/aulas");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, AulaForm.from(aulas.buscar(id)), "Editar aula", "/aulas/" + id);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") AulaForm form,
                            BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                aulas.atualizar(id, form.toRequest());
                ra.addFlashAttribute("sucesso", "Aula atualizada com sucesso.");
                return "redirect:/aulas/" + id;
            } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
                result.reject("regra", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException ex) {
                result.reject("concorrencia", "A aula foi alterada por outra operação. Recarregue e tente novamente.");
            }
        }
        return formulario(model, form, "Editar aula", "/aulas/" + id);
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusAula status, RedirectAttributes ra) {
        return executar(id, ra, "Status alterado para " + status + ".", () -> aulas.alterarStatus(id, status));
    }

    @PostMapping("/{id}/inscricoes")
    public String inscrever(@PathVariable Long id, @RequestParam Long clienteId, RedirectAttributes ra) {
        return executar(id, ra, "Cliente inscrito.", () -> aulas.inscrever(id, clienteId));
    }

    @PostMapping("/{id}/inscricoes/{clienteId}/cancelar")
    public String cancelarInscricao(@PathVariable Long id, @PathVariable Long clienteId, RedirectAttributes ra) {
        return executar(id, ra, "Inscrição cancelada.", () -> aulas.cancelarInscricao(id, clienteId));
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            aulas.excluir(id);
            ra.addFlashAttribute("sucesso", "Aula excluída.");
            return "redirect:/aulas";
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/aulas/" + id;
        }
    }

    // ---------- auxiliares

    private String formulario(Model model, AulaForm form, String titulo, String acao) {
        model.addAttribute("form", form);
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        model.addAttribute("esportes", cadastros.esportesAtivos());
        model.addAttribute("personais", cadastros.personais());
        model.addAttribute("unidades", cadastros.unidades());
        return "aulas/form";
    }

    private String executar(Long id, RedirectAttributes ra, String sucesso, Runnable acao) {
        try {
            acao.run();
            ra.addFlashAttribute("sucesso", sucesso);
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        } catch (ObjectOptimisticLockingFailureException ex) {
            ra.addFlashAttribute("erro", "A aula foi alterada por outra operação. Tente novamente.");
        }
        return "redirect:/aulas/" + id;
    }
}
