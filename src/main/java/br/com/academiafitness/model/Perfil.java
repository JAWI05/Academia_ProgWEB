package br.com.academiafitness.model;

/** Perfis de acesso. ADMIN: tudo. OPERADOR (recepção): consulta, cadastra clientes e inscreve em aulas. */
public enum Perfil {
    ADMIN("Administrador"),
    OPERADOR("Operador");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
