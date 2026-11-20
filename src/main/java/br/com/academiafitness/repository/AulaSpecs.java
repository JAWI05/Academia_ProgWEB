package br.com.academiafitness.repository;

import br.com.academiafitness.filtro.AulaFiltro;
import br.com.academiafitness.model.Aula;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

/** Monta a consulta de aulas combinando só os critérios informados (Criteria API, sem concatenar SQL). */
public final class AulaSpecs {

    private AulaSpecs() {
    }

    public static Specification<Aula> filtrar(AulaFiltro f) {
        Specification<Aula> spec = (root, query, cb) -> cb.conjunction();

        if (f.temTexto()) {
            String padrao = padraoLike(f.getQ());
            spec = spec.and((root, query, cb) -> cb.or(
                    like(cb, root.get("esporte").<String>get("nome"), padrao),
                    like(cb, root.get("personal").<String>get("nome"), padrao),
                    like(cb, root.get("unidade").<String>get("nome"), padrao)));
        }
        if (f.getEsporteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("esporte").get("id"), f.getEsporteId()));
        }
        if (f.getPersonalId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("personal").get("id"), f.getPersonalId()));
        }
        if (f.getUnidadeId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("unidade").get("id"), f.getUnidadeId()));
        }
        if (f.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), f.getStatus()));
        }
        if (f.getDe() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.<java.time.LocalDateTime>get("inicio"), f.getDe().atStartOfDay()));
        }
        if (f.getAte() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.<java.time.LocalDateTime>get("inicio"), f.getAte().plusDays(1).atStartOfDay()));
        }
        if (f.isApenasComVagas()) {
            spec = spec.and((root, query, cb) -> cb.lessThan(
                    cb.size(root.<Collection<?>>get("inscritos")),
                    root.<Integer>get("capacidade")));
        }
        return spec;
    }

    private static Predicate like(CriteriaBuilder cb, Expression<String> campo, String padrao) {
        return cb.like(cb.lower(campo), padrao, '\\');
    }

    /** %texto% em minúsculas, com os curingas do usuário (%, _) tratados como texto comum. */
    static String padraoLike(String texto) {
        String limpo = texto.trim().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + limpo + "%";
    }
}
