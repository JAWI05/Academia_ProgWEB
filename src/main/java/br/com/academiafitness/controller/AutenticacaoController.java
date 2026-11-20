package br.com.academiafitness.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AutenticacaoController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Sem restrição de método: o Spring Security encaminha para cá mantendo o método original (GET ou POST). */
    @RequestMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }
}
