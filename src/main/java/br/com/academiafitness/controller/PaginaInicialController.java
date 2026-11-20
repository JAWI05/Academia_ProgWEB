package br.com.academiafitness.controller;

import br.com.academiafitness.dto.AulaResponse;
import br.com.academiafitness.model.StatusAula;
import br.com.academiafitness.service.AulaService;
import br.com.academiafitness.service.CadastroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PaginaInicialController {

    private final AulaService aulas;
    private final CadastroService cadastros;

    public PaginaInicialController(AulaService aulas, CadastroService cadastros) {
        this.aulas = aulas;
        this.cadastros = cadastros;
    }

    @GetMapping({"/", "/home"})
    public String inicio(Model model) {
        List<AulaResponse> agendadas = aulas.listar(StatusAula.AGENDADA);
        model.addAttribute("totalAgendadas", agendadas.size());
        model.addAttribute("proximasAulas", agendadas.stream().limit(5).toList());
        model.addAttribute("totalClientes", cadastros.totalClientes());
        model.addAttribute("totalPersonais", cadastros.totalPersonais());
        model.addAttribute("totalEsportes", cadastros.totalEsportesAtivos());
        return "index";
    }
}
