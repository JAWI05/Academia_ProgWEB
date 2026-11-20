package br.com.academiafitness.exception;

import br.com.academiafitness.controller.AulaWebController;
import br.com.academiafitness.controller.ClienteWebController;
import br.com.academiafitness.controller.PaginaInicialController;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Páginas de erro amigáveis para o site (a API REST tem o próprio GlobalExceptionHandler). */
@ControllerAdvice(assignableTypes = {AulaWebController.class, ClienteWebController.class, PaginaInicialController.class})
public class WebExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(RecursoNaoEncontradoException ex, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", ex.getMessage());
        return "error";
    }
}
