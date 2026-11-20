package br.com.academiafitness.filtro;

/** Filtro de clientes pela existência de inscrições em aulas. */
public enum SituacaoCliente {
    TODOS("Todos"),
    COM_INSCRICAO("Com inscrição em aula"),
    SEM_INSCRICAO("Sem nenhuma inscrição");

    private final String rotulo;

    SituacaoCliente(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
